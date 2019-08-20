package com.sunbox.plugin;


import com.sunbox.element.Root;

import java.util.List;

public abstract class AsyncStorePlugin implements StorePlugin{

    public abstract boolean saveRoots(List<Root> roots);

    public abstract List<Root> getRoots();

}
