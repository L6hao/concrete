package cc.coodex.concrete.jaxrs.struct;

import cc.coodex.concrete.jaxrs.BigString;
import cc.coodex.concrete.jaxrs.PathParam;
import cc.coodex.concrete.common.struct.AbstractUnit;

import javax.ws.rs.HttpMethod;
import java.lang.reflect.Method;

import static cc.coodex.concrete.jaxrs.JaxRSHelper.isPrimitive;
import static cc.coodex.concrete.jaxrs.Predicates.getHttpMethod;
import static cc.coodex.concrete.jaxrs.Predicates.getRESTFulPath;

/**
 * Created by davidoff shen on 2016-11-30.
 */
public class Unit extends AbstractUnit<Param, Module> {


    private Param[] parameters;
    private String name;

    public Unit(Method method, Module module) {
        super(method, module);
        int paramCount = method.getParameterTypes().length;
        parameters = new Param[paramCount];
        for (int i = 0; i < paramCount; i++) {
            parameters[i] = new Param(method, i);
        }
        name = getNameOnInit();
        validation();
    }

    private void validation() {
        int pojoCount = 0;
        for (Param param : parameters) {
            if (!isPrimitive(param.getType()) ||
                    (param.getType() == String.class && param.getAnnotation(BigString.class) != null))
                pojoCount++;

        }

        String httpMethod = getInvokeType();
        int pojoLimited = 0;
        if (httpMethod.equalsIgnoreCase(HttpMethod.POST)
                || httpMethod.equalsIgnoreCase(HttpMethod.PUT)
                || httpMethod.equalsIgnoreCase(HttpMethod.DELETE)) {
            pojoLimited = 1;
        }
//
//        switch (httpMethod) {
//            case HttpMethod.POST:
//            case HttpMethod.PUT:
//                pojoLimited = 1;
//                break;
//            case HttpMethod.DELETE: // ?是否允许pojo
//            case HttpMethod.GET:
//                pojoLimited = 0;
//        }

        if (pojoCount > pojoLimited) {
            StringBuilder builder = new StringBuilder();
            builder.append("Object parameter count limited ").append(pojoLimited).append(" in HttpMethod.")
                    .append(httpMethod).append(", ").append(pojoCount).append(" used in ")
                    .append(getMethod().toGenericString());
            throw new RuntimeException(builder.toString());
        }

//        if(pojoCount >= 2)
//            throw new IllegalArgumentException("too many POJO parameters.")
//        String httpMethod =
    }

    private String getNameOnInit() {
        String methodName = getRESTFulPath(getMethod());

        StringBuffer buffer = new StringBuffer();

        if (methodName != null)
            buffer.append("/").append(methodName);

        for (Param parameter : getParameters()) {
            String pathParamValue = getPathParam(parameter);
            if (pathParamValue != null) {
                String restfulNode = "/{" + pathParamValue + "}";
                if (methodName == null || methodName.indexOf(restfulNode) < 0) {
                    buffer.append("/{").append(pathParamValue).append("}");
                }
            }
        }
        return buffer.toString();
    }


    protected String getPathParam(Param parameter) {
        PathParam pathParam = parameter.getAnnotation(PathParam.class);
        if (pathParam != null) return pathParam.value();
        javax.ws.rs.PathParam pathParam1 = parameter.getAnnotation(javax.ws.rs.PathParam.class);
        if (pathParam1 != null) return pathParam1.value();
        Class<?> clz = parameter.getType();
        boolean isBigString = parameter.getAnnotation(BigString.class) != null;
        //大字符串
        if (clz == String.class && isBigString) return null;

        return isPrimitive(clz) ? parameter.getName() : null;
//
//        return pathParam1 == null ?
//                (isPrimitive(parameter.getType())
//                        && (parameter.getType() == String.class
//                        && parameter.getAnnotation(BigString.class) == null)
//                        ? parameter.getName() : null) :
//                pathParam1.value();
    }


    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getInvokeType() {
        return getHttpMethod(getMethod());
    }

    @Override
    public Param[] getParameters() {
        return parameters;
    }

    @Override
    public int compareTo(AbstractUnit o) {
        int v = getName().replaceAll("\\{[^{}]*}", "").compareTo(o.getName().replaceAll("\\{[^{}]*}", ""));
        if (v == 0)
            v = getName().compareTo(o.getName());
        return v == 0 ? getInvokeType().compareTo(o.getInvokeType()) : v;
    }
}
