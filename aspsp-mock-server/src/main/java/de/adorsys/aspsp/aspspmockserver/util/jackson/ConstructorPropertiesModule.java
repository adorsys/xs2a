package de.adorsys.aspsp.aspspmockserver.util.jackson;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.json.PackageVersion;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedConstructor;
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.beans.ConstructorProperties;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;

public class ConstructorPropertiesModule extends SimpleModule {

    public ConstructorPropertiesModule() {
        super(PackageVersion.VERSION);
    }

    @Override
    public void setupModule(Module.SetupContext context) {
        super.setupModule(context);
        context.insertAnnotationIntrospector(new ConstructorPropertiesAnnotationIntrospector());
    }

    public static class ConstructorPropertiesAnnotationIntrospector extends NopAnnotationIntrospector {

        @Override
        public boolean hasCreatorAnnotation(Annotated a) {
            if (!(a instanceof AnnotatedConstructor)) {
                return false;
            }

            AnnotatedConstructor ac = (AnnotatedConstructor) a;

            Constructor<?> c = ac.getAnnotated();
            ConstructorProperties properties = c.getAnnotation(ConstructorProperties.class);

            if (properties == null) {
                return false;
            }

            for (int i = 0; i < ac.getParameterCount(); i++) {
                final String name = properties.value()[i];
                final int index = i;
                JsonProperty jsonProperty = new JsonProperty() {

                    @Override
                    public String value() {
                        return name;
                    }

                    @Override
                    public boolean required() {
                        return false;
                    }

                    @Override
                    public Class<? extends Annotation> annotationType() {
                        return JsonProperty.class;
                    }

                    @Override
                    public int index() {
                        return index;
                    }

                    @Override
                    public String defaultValue() {
                        return "";
                    }

                    @Override
                    public Access access() {
                        return Access.AUTO;
                    }
                };
                ac.getParameter(i).addOrOverride(jsonProperty);
            }
            return true;
        }

    }

}
