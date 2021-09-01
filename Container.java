package com.example.course.ioc;






import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Container {
    private final Map<String, Object> objectFactory = new HashMap<>();

    public static void start() throws Exception{
        Container c = new Container();
        List<Class<?>> classes = c.scan();
        c.register(classes);
        c.autowiredObjects(classes);
    }

    private List<Class<?>> scan() {
        return Arrays.asList(StudentRegisterServiceimpl1.class,StudentRegisterServiceimpl2.class, StudentApplication.class, Starter.class);
    }

    private boolean register(List<Class<?>> classes) throws  Exception {
        for(Class<?> clazz: classes) {
            Annotation[] annotations = clazz.getAnnotations();
            for(Annotation a: annotations) {
                if(a.annotationType() == Component.class) {
                    objectFactory.put(clazz.getSimpleName(), clazz.getDeclaredConstructor(null).newInstance());
                    Class<?>[] tmp = clazz.getInterfaces();
                    if (tmp.length != 0) {
                        String DuplicateNameCheck = tmp[0].getSimpleName();
                        if (objectFactory.containsKey(DuplicateNameCheck)) {
                            objectFactory.put(DuplicateNameCheck, null);             //If we have multiple interface, set value to null
                        }else {
                            objectFactory.put(DuplicateNameCheck, clazz.getDeclaredConstructor(null).newInstance());
                        }

                    }
                }
            }
        }
        return true;
    }

    private boolean autowiredObjects(List<Class<?>> classes) throws Exception{
        for(Class<?> clazz: classes) {
            Field[] fields = clazz.getDeclaredFields();
            Object curInstance = objectFactory.get(clazz.getSimpleName());
            for(Field f: fields) {
                Annotation[] annotations = f.getAnnotations();
                    if(annotations[0].annotationType() == autowired.class) {
                        Class<?> type = f.getType();
                        Object autowiredInstance = objectFactory.get(type.getSimpleName());
                        if (autowiredInstance == null){
                                if (annotations[1].annotationType() != Qualifier.class) {
                                        Exception MultipleImplementationException = new Exception();
                                        throw  MultipleImplementationException;
                                }
                                    Qualifier q = (Qualifier) annotations[1];
                                    String keyword = q.value();
                                    autowiredInstance = objectFactory.get(keyword);
                                    if (autowiredInstance == null) {
                                        throw new NullPointerException();          //If we have multiple implementations of same type and can't inject by name, throw exception.
                                     }
                                f.setAccessible(true);
                                f.set(curInstance, autowiredInstance);
                                continue;
                        }
                        f.setAccessible(true);
                        f.set(curInstance, autowiredInstance);
                    }


            }
        }
        return true;
    }
}


@Component
interface StudentService{
    void print();
}

@Component
class StudentRegisterServiceimpl1 implements StudentService {
    @Override
    public String toString() {
        return "this is student register service instance : " + super.toString() + "\n";
    }

    @Override
    public void print() {
        System.out.println("this is student register service instance : " + super.toString() + "\n");
    }
}
@Component
class StudentRegisterServiceimpl2 implements StudentService {
    @Override
    public String toString() {
        return "this is student register service instance : " + super.toString() + "\n";
    }

    @Override
    public void print() {
        System.out.println("this is student register service instance : " + super.toString() + "\n");
    }
}
@Component
class StudentApplication  {
    @autowired
    @Qualifier(value = "StudentRegisterServiceimpl1")
    StudentService StudentService1;

    @autowired
    @Qualifier(value = "StudentRegisterServiceimpl2")
    StudentService StudentService2;

//    @autowired
//    public StudentApplication(StudentService ss) {
//        this.StudentService1 = ss;
//
//    }


    @Override
    public String toString() {
        return "StudentApplication{\n" +
                "studentRegisterService=" + StudentService1  +
                "}\n" + "StudentApplication{\n" +
                "studentRegisterService=" + StudentService2  +
                "}\n";
    }

}



@Component
class Starter  {
    @autowired
    private static StudentApplication studentApplication;

    @autowired
    @Qualifier(value = "StudentRegisterServiceimpl2")
    private static StudentService studentService2;
    @autowired
    @Qualifier(value = "StudentRegisterServiceimpl1")
    private static StudentService studentService1;
    Starter() {

    }

    public static void main(String[] args) throws Exception{
        Container.start();
        System.out.println(studentApplication);
        System.out.println(studentService2);
        System.out.println(studentService1);
        studentService1.print();
    }
}
/**
 *  1. add interface
 *  2. all components need to impl interface
 *  3. @Autowired -> inject by type
 *                   if we have multiple implementations of current type => throw exception
 *  4. @Autowired + @Qualifier("name") -> inject by bean name
 *  5. provide constructor injection
 *      @Autowired
 *      public xx(.. ,..) {
 *          .. = ..
 *          .. = ..
 *      }
 *  6. provide setter injection
 *  7. provide different injection scope / bean scope
 *          1. now we only supporting singleton
 *          2. prototype -> @Autowired => you inject a new instance
 */