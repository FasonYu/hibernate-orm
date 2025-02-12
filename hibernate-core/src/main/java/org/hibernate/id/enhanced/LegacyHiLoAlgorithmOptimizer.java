/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.id.enhanced;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.hibernate.HibernateException;
import org.hibernate.id.IntegralDataTypeHolder;

import org.jboss.logging.Logger;

/**
 * Slight variation from {@link HiLoOptimizer}, maintaining compatibility with the values generated by the
 * legacy Hibernate hilo based generators.
 *
 * @author Steve Ebersole
 */
public class LegacyHiLoAlgorithmOptimizer extends AbstractOptimizer {
	private static final Logger log = Logger.getLogger( LegacyHiLoAlgorithmOptimizer.class );

	private final long initialMaxLo;

	private static class GenerationState {
		private long maxLo;
		private long lo;
		private IntegralDataTypeHolder hi;

		private IntegralDataTypeHolder lastSourceValue;
		private IntegralDataTypeHolder value;
	}

	/**
	 * Constructs a {@code LegacyHiLoAlgorithmOptimizer}
	 *
	 * @param returnClass The Java type of the values to be generated
	 * @param incrementSize The increment size.
	 */
	public LegacyHiLoAlgorithmOptimizer(Class<?> returnClass, int incrementSize) {
		super( returnClass, incrementSize );
		if ( incrementSize < 1 ) {
			throw new HibernateException( "increment size cannot be less than 1" );
		}
		if ( log.isTraceEnabled() ) {
			log.tracev( "Creating hilo optimizer (legacy) with [incrementSize={0}; returnClass={1}]", incrementSize, returnClass.getName() );
		}
		initialMaxLo = incrementSize;
	}

	@Override
	public synchronized Serializable generate(AccessCallback callback) {
		final GenerationState generationState = locateGenerationState( callback.getTenantIdentifier() );

		if ( generationState.lo > generationState.maxLo ) {
			generationState.lastSourceValue = callback.getNextValue();
			generationState.lo = generationState.lastSourceValue.eq( 0 ) ? 1 : 0;
			generationState.hi = generationState.lastSourceValue.copy().multiplyBy( generationState.maxLo + 1 );
		}
		generationState.value = generationState.hi.copy().add( generationState.lo++ );
		return generationState.value.makeValue();
	}

	private GenerationState noTenantState;
	private Map<String,GenerationState> tenantSpecificState;

	private GenerationState locateGenerationState(String tenantIdentifier) {
		if ( tenantIdentifier == null ) {
			if ( noTenantState == null ) {
				noTenantState = createGenerationState();
			}
			return noTenantState;
		}
		else {
			GenerationState state;
			if ( tenantSpecificState == null ) {
				tenantSpecificState = new ConcurrentHashMap<>();
				state = createGenerationState();
				tenantSpecificState.put( tenantIdentifier, state );
			}
			else {
				state = tenantSpecificState.get( tenantIdentifier );
				if ( state == null ) {
					state = createGenerationState();
					tenantSpecificState.put( tenantIdentifier, state );
				}
			}
			return state;
		}
	}

	private GenerationState createGenerationState() {
		final GenerationState state = new GenerationState();
		state.maxLo = initialMaxLo;
		state.lo = initialMaxLo + 1;
		return state;
	}

	private GenerationState noTenantGenerationState() {
		if ( noTenantState == null ) {
			throw new IllegalStateException( "Could not locate previous generation state for no-tenant" );
		}
		return noTenantState;
	}

	@Override
	public synchronized IntegralDataTypeHolder getLastSourceValue() {
		return noTenantGenerationState().lastSourceValue.copy();
	}

	@Override
	public boolean applyIncrementSizeToSourceValues() {
		return false;
	}

	/**
	 * Getter for property 'lastValue'.
	 * <p>
	 * Exposure intended for testing purposes.
	 *
	 * @return Value for property 'lastValue'.
	 */
	public synchronized IntegralDataTypeHolder getLastValue() {
		return noTenantGenerationState().value;
	}
}
