/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.delete;

import java.util.Map;
import java.util.Set;

import org.hibernate.query.criteria.JpaCriteriaDelete;
import org.hibernate.query.sqm.NodeBuilder;
import org.hibernate.query.sqm.SemanticQueryWalker;
import org.hibernate.query.sqm.SqmQuerySource;
import org.hibernate.query.sqm.tree.AbstractSqmRestrictedDmlStatement;
import org.hibernate.query.sqm.tree.SqmCopyContext;
import org.hibernate.query.sqm.tree.SqmDeleteOrUpdateStatement;
import org.hibernate.query.sqm.tree.cte.SqmCteStatement;
import org.hibernate.query.sqm.tree.expression.SqmParameter;
import org.hibernate.query.sqm.tree.from.SqmRoot;
import org.hibernate.query.sqm.tree.predicate.SqmWhereClause;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;

/**
 * @author Steve Ebersole
 */
public class SqmDeleteStatement<T>
		extends AbstractSqmRestrictedDmlStatement<T>
		implements SqmDeleteOrUpdateStatement<T>, JpaCriteriaDelete<T> {

	public SqmDeleteStatement(SqmRoot<T> target, SqmQuerySource querySource, NodeBuilder nodeBuilder) {
		super( target, querySource, nodeBuilder );
	}

	public SqmDeleteStatement(Class<T> targetEntity, SqmQuerySource querySource, NodeBuilder nodeBuilder) {
		super(
				new SqmRoot<>(
						nodeBuilder.getDomainModel().entity( targetEntity ),
						null,
						false,
						nodeBuilder
				),
				querySource,
				nodeBuilder
		);
	}

	public SqmDeleteStatement(
			NodeBuilder builder,
			SqmQuerySource querySource,
			Set<SqmParameter<?>> parameters,
			Map<String, SqmCteStatement<?>> cteStatements,
			SqmRoot<T> target) {
		super( builder, querySource, parameters, cteStatements, target );
	}

	@Override
	public SqmDeleteStatement<T> copy(SqmCopyContext context) {
		final SqmDeleteStatement<T> existing = context.getCopy( this );
		if ( existing != null ) {
			return existing;
		}
		final SqmDeleteStatement<T> statement = context.registerCopy(
				this,
				new SqmDeleteStatement<>(
						nodeBuilder(),
						getQuerySource(),
						copyParameters( context ),
						copyCteStatements( context ),
						getTarget().copy( context )
				)
		);
		statement.setWhereClause( copyWhereClause( context ) );
		return statement;
	}

	@Override
	public SqmDeleteStatement<T> where(Expression<Boolean> restriction) {
		setWhere( restriction );
		return this;
	}

	@Override
	public SqmDeleteStatement<T> where(Predicate... restrictions) {
		setWhere( restrictions );
		return this;
	}

	@Override
	public <X> X accept(SemanticQueryWalker<X> walker) {
		return walker.visitDeleteStatement( this );
	}

	@Override
	public void appendHqlString(StringBuilder sb) {
		appendHqlCteString( sb );
		sb.append( "delete from " );
		sb.append( getTarget().getEntityName() );
		sb.append( ' ' ).append( getTarget().resolveAlias() );
		super.appendHqlString( sb );
	}
}
