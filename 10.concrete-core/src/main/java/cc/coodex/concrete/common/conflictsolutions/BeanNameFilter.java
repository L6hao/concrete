package cc.coodex.concrete.common.conflictsolutions;

import cc.coodex.concrete.common.ConcreteException;
import cc.coodex.concrete.common.ConcreteHelper;
import cc.coodex.concrete.common.ConflictSolution;
import cc.coodex.concrete.common.ErrorCodes;
import cc.coodex.util.Common;
import cc.coodex.util.Profile;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 基于Bean的名字过滤出唯一的实现
 * Created by davidoff shen on 2016-11-01.
 */
public class BeanNameFilter implements ConflictSolution {

    private static final Profile profile = ConcreteHelper.getProfile();

    private Set<String> filter(Set<String> set) {
        String prefix = profile.getString(BeanNameFilter.class.getCanonicalName() + ".prefix");
        if (Common.isBlank(prefix)) return set;

        Set<String> stringSet = new HashSet<String>();
        for (String str : set) {
            if (str != null && str.startsWith(prefix))
                stringSet.add(str);
        }
        return stringSet;
    }

    @Override
    public boolean accepted(Class<?> clazz) {
        return true;
    }

    @Override
    public <T> T conflict(Map<String, T> beans, Class<T> clz) {
        Set<String> set = filter(beans.keySet());
        switch (set.size()) {
            case 0:
                throw new ConcreteException(ErrorCodes.NO_SERVICE_INSTANCE_FOUND, clz);
            case 1:
                return beans.get(set.iterator().next());
            default:
                throw new ConcreteException(ErrorCodes.BEAN_CONFLICT, clz, set.size());
        }
    }
}
