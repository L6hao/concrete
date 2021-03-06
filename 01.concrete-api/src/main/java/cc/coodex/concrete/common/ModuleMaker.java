package cc.coodex.concrete.common;

import cc.coodex.concrete.common.struct.AbstractModule;

/**
 * Created by davidoff shen on 2016-11-30.
 */
public interface ModuleMaker<MODULE extends AbstractModule> {

    boolean isAccept(String desc);

    MODULE make(Class<?> interfaceClass);
}
