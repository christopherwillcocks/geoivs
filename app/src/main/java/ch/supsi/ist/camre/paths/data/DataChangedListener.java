package ch.supsi.ist.camre.paths.data;

import java.io.Serializable;

public interface DataChangedListener {
    public void dataChanged(Serializable serializable);
}
