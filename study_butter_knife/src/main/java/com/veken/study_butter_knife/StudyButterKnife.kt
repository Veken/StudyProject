object StudyButterKnife {

    fun bind(obj: Any){

        try {
            var clsName: String = target.getClass().getName()
            var bindingClass: Class<?> = Class.forName(clsName + "_ViewBinding");
            try {
                var constructor:Constructor = bindingClass.getConstructor(target.getClass());
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

}