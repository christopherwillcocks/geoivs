package ch.supsi.ist.camre.paths.utils;

import android.app.Fragment;
import ch.supsi.ist.camre.paths.data.Path;

public interface OnPathModifiedListener {
    public void onPathModified(Path path, Fragment fragment);
}