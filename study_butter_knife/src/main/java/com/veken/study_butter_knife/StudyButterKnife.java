package com.veken.study_butter_knife;

import android.app.Activity;
import android.view.View;


import com.veken.lib_process.annotaions.BindView;
import com.veken.lib_process.annotaions.OnClick;
import com.veken.lib_process.annotaions.OnLongClick;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by Veken on 2023/10/30 15:54
 *
 * @desc
 */
class StudyButterKnife {

//    public static void bind(Activity target) {
//        bindViews(target, target.getClass().getDeclaredFields(), target.findViewById(android.R.id.content));
//        createOnClick(target, target.getClass().getDeclaredMethods(), target.findViewById(android.R.id.content));
//        createOnLongClick(target, target.getClass().getDeclaredMethods(), target.findViewById(android.R.id.content));
//    }

    public static void bind(Activity target){
            try {
                String clsName = target.getClass().getCanonicalName();
                Class<?> bindingClass  = Class.forName(clsName + "_ViewBinding");
                Class activityClass = Class.forName(target.getClass().getCanonicalName());
                try {
                    Constructor constructor = bindingClass.getDeclaredConstructor(activityClass);
                    try {
                        System.out.println("constructor-----------newInstance- before");
                        constructor.newInstance(target);
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
    }

    public static void bind(final Object obj, View promptsView){
        bindViews(obj, obj.getClass().getDeclaredFields(), promptsView);
        createOnClick(obj, obj.getClass().getDeclaredMethods(), promptsView);
        createOnLongClick(obj, obj.getClass().getDeclaredMethods(), promptsView);
    }

    private static void createOnLongClick(Object target, Method[] declaredMethods, View rootView) {
        for (Method method : declaredMethods) {
            Annotation annotation = method.getAnnotation(OnLongClick.class);
            if (annotation != null) {
                OnLongClick onClick = (OnLongClick) annotation;
                int id = onClick.value();
                View view = rootView.findViewById(id);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            method.invoke(target);
                            method.setAccessible(true);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                });

            }
        }

    }

    private static void createOnClick(Object target, Method[] declaredMethods, View rootView) {
        for (Method method : declaredMethods) {
            Annotation annotation = method.getAnnotation(OnClick.class);
            if (annotation != null) {
                OnClick onClick = (OnClick) annotation;
                int id = onClick.value();
                View view = rootView.findViewById(id);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            method.invoke(target);
                            method.setAccessible(true);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                });

            }
        }
    }

    private static void bindViews(Object target, Field[] declaredFields, View rootView) {
        for (Field field : declaredFields) {
            Annotation annotation = field.getAnnotation(BindView.class);
            if (annotation != null) {
                BindView bindView = (BindView) annotation;
                int id = bindView.value();
                View view = rootView.findViewById(id);
                try {
                    field.setAccessible(true);
                    field.set(target, view);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
