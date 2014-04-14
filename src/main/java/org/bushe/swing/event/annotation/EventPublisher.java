package org.bushe.swing.event.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
/**
 * This annotation converts an Event that a Component usually fires to a listener into a publication on the EventBus
 * or a ContainerEventService.
 * <p>
 * This annotation removes a lot of the boilerplate in writing Swing listeners.  If you are using the EventBus,
 * then this makes things very easy, if you are not, then you might want to after using @Publish Annotations.
 * <p>
 * The typical scenario is like so:
 * <pre>
 *    public class FooModel() {
 *       String choice;
 *       String text;
 *    }
 *    public class FooView {
 *       JComboBox combo;
 *       JButton button;
 *       JTextField text;
 *
 *       public FooView () {
 *       }
 *
 *       public JComboBox getCombo() {
 *          if (combo == null) {
 *             combo = new JComboBox();
 *             combo.setName("choice");
 *          }
 *          return combo;
 *       }
 *    }
 * </pre>
 */
public @interface EventPublisher {
   //The Event
}
