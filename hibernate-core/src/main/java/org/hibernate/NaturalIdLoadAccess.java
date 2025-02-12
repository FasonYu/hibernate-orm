/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate;

import java.util.Optional;

/**
 * Loads an entity by its natural identifier.
 * <p>
 * This is a generic form of load-by-natural-id covering both a single attribute
 * and multiple attributes as the natural-id.  For natural-ids defined by a single
 * attribute, {@link SimpleNaturalIdLoadAccess} offers simplified access.
 *
 * @author Eric Dalquist
 * @author Steve Ebersole
 *
 * @see org.hibernate.annotations.NaturalId
 * @see Session#byNaturalId
 */
public interface NaturalIdLoadAccess<T> {
	/**
	 * Specify the {@link LockOptions} to use when retrieving the entity.
	 *
	 * @param lockOptions The lock options to use.
	 *
	 * @return {@code this}, for method chaining
	 */
	NaturalIdLoadAccess<T> with(LockOptions lockOptions);

	/**
	 * Add a NaturalId attribute value.
	 * 
	 * @param attributeName The entity attribute name that is marked as a NaturalId
	 * @param value The value of the attribute
	 *
	 * @return {@code this}, for method chaining
	 */
	NaturalIdLoadAccess<T> using(String attributeName, Object value);

	/**
	 * Set multiple natural-id attribute values at once.  The passed array is
	 * expected to have an even number of elements, with the attribute name followed
	 * by its value, for example, {@code using( "system", "matrix", "username", "neo" )}.
	 *
	 * @return {@code this}, for method chaining
	 */
	NaturalIdLoadAccess<T> using(Object... mappings);

	/**
	 * For entities with mutable natural ids, should natural ids be synchronized prior to performing a lookup?
	 * The default, for correctness, is to synchronize.
	 * <p>
	 * Here "synchronization" means updating the natural id to primary key cross-reference maintained by the
	 * session. When enabled, prior to performing the lookup, Hibernate will check all entities of the given
	 * type associated with the session to see if any natural id values have changed and, if so, update the
	 * cross-reference. There is a performance penalty associated with this, so if it is completely certain
	 * the no natural id in play has changed, this setting can be disabled to circumvent that impact.
	 * Disabling this setting when natural id values <em>have</em> changed can result in incorrect results!
	 *
	 * @param enabled Should synchronization be performed?
	 *                {@code true} indicates synchronization will be performed;
	 *                {@code false} indicates it will be circumvented.
	 *
	 * @return {@code this}, for method chaining
	 */
	NaturalIdLoadAccess<T> setSynchronizationEnabled(boolean enabled);

	/**
	 * Return the persistent instance with the natural id value(s) defined by the call(s) to {@link #using}.  This
	 * method might return a proxied instance that is initialized on-demand, when a non-identifier method is accessed.
	 * <p>
	 * You should not use this method to determine if an instance exists; to check for existence, use {@link #load}
	 * instead.  Use this only to retrieve an instance that you assume exists, where non-existence would be an
	 * actual error.
	 *
	 * @return the persistent instance or proxy
	 */
	T getReference();

	/**
	 * Return the persistent instance with the natural id value(s) defined by the call(s) to {@link #using}, or
	 * {@code null} if there is no such persistent instance.  If the instance is already associated with the session,
	 * return that instance, initializing it if needed.  This method never returns an uninitialized instance.
	 *
	 * @return The persistent instance or {@code null} 
	 */
	T load();

	/**
	 * Same semantic as {@link #load} except that here {@link Optional} is returned to
	 * handle nullability.
	 *
	 * @return The persistent instance, if one, wrapped in Optional
	 */
	Optional<T> loadOptional();

}
