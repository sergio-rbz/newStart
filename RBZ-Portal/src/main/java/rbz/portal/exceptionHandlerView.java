package rbz.portal;

import javax.enterprise.context.RequestScoped;
import javax.faces.FacesException;
import javax.faces.application.ViewExpiredException;
import javax.faces.context.FacesContext;
import javax.inject.Named;

@Named
@RequestScoped
public class exceptionHandlerView {
    public void throwNullPointerException() {
        throw new NullPointerException("A NullPointerException!");
    }

    public void throwWrappedIllegalStateException() {
        Throwable t = new IllegalStateException("A wrapped IllegalStateException!");
        throw new FacesException(t);
    }

    public void throwViewExpiredException() {
        throw new ViewExpiredException("A ViewExpiredException!",
                FacesContext.getCurrentInstance().getViewRoot().getViewId());
    }

}
