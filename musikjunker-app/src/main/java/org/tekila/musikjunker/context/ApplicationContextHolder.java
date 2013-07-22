package org.tekila.musikjunker.context;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class ApplicationContextHolder implements ApplicationContextAware {
    private static ApplicationContext value;

    public static ApplicationContext getValue() {
            return value;
    }

    public static void setValue(ApplicationContext value) {
            ApplicationContextHolder.value = value;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
                    throws BeansException {
            setValue(applicationContext);
    }

}
