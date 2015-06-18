package k8061;

import java.util.EventListener;

import k8061.event.K8061Event;

public interface K8061Listener extends EventListener
{
	public void valueChanged(K8061Event event);
}
