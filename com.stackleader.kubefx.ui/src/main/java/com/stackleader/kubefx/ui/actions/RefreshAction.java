package com.stackleader.kubefx.ui.actions;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.google.common.collect.Sets;
import static com.stackleader.kubefx.ui.utils.FXUtilities.runAndWait;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, provide = {RefreshAction.class, KubeAction.class})
public class RefreshAction extends AbstractKubeAction implements KubeAction, Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(RefreshAction.class);
    private static final String REFRESH = "refresh";
    private Set<RefreshActionListener> listeners;

    public RefreshAction() {
        setText(REFRESH);
        listeners = Sets.newConcurrentHashSet();
    }


    @Override
    public Runnable getAction() {
        return this;
    }

    @Override
    public void run() {
        runAndWait(() -> {
            for (RefreshActionListener listener : listeners) {
                try {
                    listener.refresh();
                } catch (Throwable ex) {
                    LOG.error(ex.getMessage(), ex);
                }
            }
        });
    }

    @Reference(optional = true, multiple = true, unbind = "removeRefreshActionListener", dynamic = true)
    public void addRefreshActionListener(RefreshActionListener actionListener) {
        listeners.add(actionListener);
    }

    public void removeRefreshActionListener(RefreshActionListener actionListener) {
        listeners.remove(actionListener);
    }

}
