package phex.event;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

import org.bushe.swing.event.EventService;
import org.bushe.swing.event.annotation.BaseProxySubscriber;
import org.bushe.swing.event.annotation.EventSubscriber;
import org.bushe.swing.event.annotation.EventTopicPatternSubscriber;
import org.bushe.swing.event.annotation.EventTopicSubscriber;
import org.bushe.swing.event.annotation.ProxyTopicPatternSubscriber;
import org.bushe.swing.event.annotation.ProxyTopicSubscriber;
import org.bushe.swing.event.annotation.ReferenceStrength;
import org.bushe.swing.event.annotation.UseTheClassOfTheAnnotatedMethodsParameter;


/**
 * Enhances classes that use EventService Annotations. <p/> This class makes the
 * EventService annotations "come alive." This can be used in code like so:
 * 
 * <pre>
 * public class MyAppController {
 *   public MyAppController {
 *       AnnotationProcessor.process(this);//this line can be avoided with a compile-time tool or an Aspect
 *   }
 *   @EventSubscriber
 *   public void onAppStartingEvent(AppStartingEvent appStartingEvent) {
 *      //do something
 *   }
 *   @EventSubscriber
 *   public void onAppClosingEvent(AppClosingEvent appClosingEvent) {
 *      //do something
 *   }
 * }
 * </pre>
 * 
 * <p/> This class ignores the attached EventService specific properties 
 * of the annotation (eventServiceName, autoCreateEventServiceClass) and 
 * always uses the subscribeToService.
 */
public class EventAnnotationProcessor
{
    private EventService subscribeToService;
    
    public EventAnnotationProcessor( EventService subscribeToService )
    {
        this.subscribeToService = subscribeToService;
    }
    
    public void process(Object obj)
    {
        if ( obj == null )
        {
            return;
        }
        Class cl = obj.getClass();
        Method[] methods = cl.getMethods();
        for ( int i = 0; i < methods.length; i++ )
        {
            Method method = methods[i];
            EventSubscriber classAnnotation = method
                .getAnnotation( EventSubscriber.class );
            if ( classAnnotation != null )
            {
                process( classAnnotation, obj, method );
            }
            EventTopicSubscriber topicAnnotation = method.getAnnotation( 
                EventTopicSubscriber.class );
            if ( topicAnnotation != null )
            {
                process( topicAnnotation, obj, method );
            }
            EventTopicPatternSubscriber topicPatternAnnotation = method.getAnnotation( 
                EventTopicPatternSubscriber.class );
            if ( topicPatternAnnotation != null )
            {
                process( topicPatternAnnotation, obj, method );
            }
        }
    }

    private void process(EventTopicPatternSubscriber topicPatternAnnotation,
        Object obj, Method method)
    {
        // Check args
        String topicPattern = topicPatternAnnotation.topicPattern();
        if ( topicPattern == null )
        {
            throw new IllegalArgumentException(
                "Topic pattern cannot be null for EventTopicPatternSubscriber annotation" );
        }

        // Create proxy and subscribe
        Pattern pattern = Pattern.compile( topicPattern );
        ProxyTopicPatternSubscriber subscriber = new ProxyTopicPatternSubscriber(
            obj, method, topicPatternAnnotation.referenceStrength(),
            subscribeToService, topicPattern, pattern );

        //See Issue #18
        //Also note that this post is wrong: https://eventbus.dev.java.net/servlets/ProjectForumMessageView?messageID=19499&forumID=1834
        //Since two WeakReferences are not treated as one.  So this always has to be strong and we'll have to clean up occasionally.
        subscribeToService.subscribeStrongly(pattern, subscriber);
    }

    private void process(EventTopicSubscriber topicAnnotation, Object obj,
        Method method)
    {
        // Check args
        String topic = topicAnnotation.topic();
        if ( topic == null )
        {
            throw new IllegalArgumentException(
                "Topic cannot be null for EventTopicSubscriber annotation" );
        }

        // Create proxy and subscribe
        ProxyTopicSubscriber subscriber = new ProxyTopicSubscriber( obj,
            method, topicAnnotation.referenceStrength(), subscribeToService, topic );

        //See Issue #18
        //Also note that this post is wrong: https://eventbus.dev.java.net/servlets/ProjectForumMessageView?messageID=19499&forumID=1834
        //Since two WeakReferences are not treated as one.  So this always has to be strong and we'll have to clean up occasionally.
        subscribeToService.subscribeStrongly(topic, subscriber);
    }

    private void process(EventSubscriber annotation, Object obj, Method method)
    {
        // Check args
        Class eventClass = annotation.eventClass();
        if ( eventClass == null )
        {
            throw new IllegalArgumentException(
                "Event class cannot be null for EventSubscriber annotation" );
        }
        else if ( UseTheClassOfTheAnnotatedMethodsParameter.class
            .equals( eventClass ) )
        {
            Class[] params = method.getParameterTypes();
            if ( params.length < 1 )
            {
                throw new RuntimeException(
                    "Expected annotated method to have one parameter." );
            }
            else
            {
                eventClass = params[0];
            }
        }

        // Create proxy and subscribe
        // See
        // https://eventbus.dev.java.net/servlets/ProjectForumMessageView?messageID=19499&forumID=1834
        BaseProxySubscriber subscriber = new BaseProxySubscriber( obj, method,
            annotation.referenceStrength(), subscribeToService, eventClass );
        if ( annotation.exact() )
        {
            //See Issue #18
            //Also note that this post is wrong: https://eventbus.dev.java.net/servlets/ProjectForumMessageView?messageID=19499&forumID=1834
            //Since two WeakReferences are not treated as one.  So this always has to be strong and we'll have to clean up occasionally.
            subscribeToService.subscribeExactlyStrongly(eventClass, subscriber);
        } 
        else 
        {
            //See Issue #18
            //Also note that this post is wrong: https://eventbus.dev.java.net/servlets/ProjectForumMessageView?messageID=19499&forumID=1834
            //Since two WeakReferences are not treated as one.  So this always has to be strong and we'll have to clean up occasionally.
            subscribeToService.subscribeStrongly(eventClass, subscriber);
        }
    }
}