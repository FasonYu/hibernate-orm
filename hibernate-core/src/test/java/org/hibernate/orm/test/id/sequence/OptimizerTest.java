/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.orm.test.id.sequence;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.testing.TestForIssue;
import org.hibernate.testing.orm.junit.DomainModel;
import org.hibernate.testing.orm.junit.SessionFactory;
import org.hibernate.testing.orm.junit.SessionFactoryScope;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * @author Steve Ebersole
 */
@DomainModel(
		annotatedClasses = OptimizerTest.TheEntity.class
)
@SessionFactory
public class OptimizerTest {

	@Test
	@TestForIssue(jiraKey = "HHH-10166")
	public void testGenerationPastBound(SessionFactoryScope scope) {
		scope.inTransaction(
				session -> {
					for ( int i = 0; i < 100; i++ ) {
						TheEntity entity = new TheEntity( Integer.toString( i ) );
						session.save( entity );
					}
				}
		);

		scope.inTransaction(
				session -> {
					TheEntity number100 = session.get( TheEntity.class, 100 );
					assertThat( number100, notNullValue() );
					session.createQuery( "delete TheEntity" ).executeUpdate();
				}
		);
	}

	@Entity(name = "TheEntity")
	@Table(name = "TheEntity")
	public static class TheEntity {
		@Id
		@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq1")
		@SequenceGenerator(name = "seq1", sequenceName = "the_sequence")
		public Integer id;
		public String someString;

		public TheEntity() {
		}

		public TheEntity(String someString) {
			this.someString = someString;
		}
	}
}
