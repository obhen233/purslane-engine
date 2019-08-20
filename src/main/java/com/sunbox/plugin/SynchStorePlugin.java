package com.sunbox.plugin;

import com.sunbox.element.Root;

import java.util.List;

public abstract class SynchStorePlugin implements StorePlugin{

    public abstract boolean saveRoot(Root root ,boolean updateFlag);

    public abstract boolean deleteRoot(Root root);

    public abstract List<Root> getRoots();

}
