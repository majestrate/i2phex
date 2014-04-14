package phex.event;

import org.bushe.swing.event.ThreadSafeEventService;

public class PhexEventServiceImpl extends ThreadSafeEventService 
    implements PhexEventService
{
    private final EventAnnotationProcessor annotationProcessor;
    
    public PhexEventServiceImpl()
    {
        annotationProcessor = new EventAnnotationProcessor( this );
    }
    
    public void processAnnotations( Object obj )
    {
        annotationProcessor.process( obj );
    }
    
}
