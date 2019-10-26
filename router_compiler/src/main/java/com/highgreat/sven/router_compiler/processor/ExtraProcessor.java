package com.highgreat.sven.router_compiler.processor;

import com.google.auto.service.AutoService;
import com.highgreat.sven.router_annotation.Extra;
import com.highgreat.sven.router_compiler.utils.Consts;
import com.highgreat.sven.router_compiler.utils.LoadExtraBuilder;
import com.highgreat.sven.router_compiler.utils.Log;
import com.highgreat.sven.router_compiler.utils.Utils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import static javax.lang.model.element.Modifier.PUBLIC;


//自定义注解处理器

@AutoService(Processor.class)//自动注册自定义注解处理器
@SupportedOptions(Consts.ARGUMENTS_NAME)//注解处理器支持的注解操作
@SupportedSourceVersion(SourceVersion.RELEASE_7)//支持的java版本
@SupportedAnnotationTypes(Consts.ANN_TYPE_Extra) //注解处理器可以支持的注解类型
public class ExtraProcessor extends AbstractProcessor {

    /**
     * 节点工具类 (类、函数、属性都是节点)
     */
    private Elements elementUtils;

    /**
     * type(类信息)工具类
     */
    private Types typeUtils;
    /**
     * 类/资源生成器
     */
    private Filer filerUtils;
    /**
     * 记录所有需要注入的属性 key:类节点 value:需要注入的属性节点集合
     */
    private Map<TypeElement, List<Element>> parentAndChild = new HashMap<>();

    private Log log;
    /**
     * 初始化 从 {@link ProcessingEnvironment} 中获得一系列处理器工具
     * @param processingEnvironment
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        //获得apt的日志输出
        log = Log.newLog(processingEnvironment.getMessager());
        elementUtils = processingEnv.getElementUtils();
        typeUtils = processingEnvironment.getTypeUtils();
        filerUtils = processingEnv.getFiler();
    }

    /**
     *
     * @param annotations
     * @param roundEnv 表示当前或是之前的运行环境,可以通过该对象查找找到的注解。
     * @return true 表示后续处理器不会再处理(已经处理)
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if(!Utils.isEmpty(annotations)){
            Set<? extends Element> elementsAnnotatedWith = roundEnv.
                    getElementsAnnotatedWith(Extra.class);
            if(!Utils.isEmpty(elementsAnnotatedWith)){
                try {
                    categories(elementsAnnotatedWith);
                    generateAutoWired();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return true;
        }
        return false;
    }

    private void generateAutoWired() throws IOException {

        TypeMirror type_Activity = elementUtils.getTypeElement(Consts.ACTIVITY).asType();
        TypeElement IExtra = elementUtils.getTypeElement(Consts.IEXTRA);

        ParameterSpec target = ParameterSpec.builder(TypeName.OBJECT, "target").build();
        if(!Utils.isEmpty(parentAndChild)){
            // 遍历所有需要注入的 类:属性
            for (Map.Entry<TypeElement, List<Element>> entry : parentAndChild.entrySet()) {
                TypeElement key = entry.getKey();
                if(!typeUtils.isSubtype(key.asType(),type_Activity)){
                    throw new RuntimeException("[Just Support Activity Field]:" +
                            key);
                }
                //封装的函数生成类
                LoadExtraBuilder loadExtra = new LoadExtraBuilder(target);
                loadExtra.setElementUtils(elementUtils);
                loadExtra.setTypeUtils(typeUtils);
                ClassName className = ClassName.get(key);
                loadExtra.injectTarget(className);
                //遍历属性
                for (int i = 0; i < entry.getValue().size(); i++) {
                    Element element = entry.getValue().get(i);
                    loadExtra.buildStatement(element);
                }

                //生成java类名
                String extraClassName = key.getSimpleName()+Consts.NAME_OF_EXTRA;
                // 生成 XX$$Autowired
                JavaFile.builder(className.packageName(), TypeSpec.classBuilder(extraClassName)
                        .addSuperinterface(ClassName.get(IExtra))
                        .addModifiers(PUBLIC).addMethod(loadExtra.build()).build())
                        .build().writeTo(filerUtils);
                log.i("Generated Extra: " + className.packageName() + "." + extraClassName);
            }
        }
    }

    /**
     * 记录需要生成的类与属性
     * @param elementsAnnotatedWith
     */
    private void categories(Set<? extends Element> elementsAnnotatedWith) {

        for (Element element : elementsAnnotatedWith) {
            //获得父节点(类)
            TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
            if(parentAndChild.containsKey(enclosingElement)){
                parentAndChild.get(enclosingElement).add(element);
            }else{
                List<Element> childs = new ArrayList<>();
                childs.add(element);
                parentAndChild.put(enclosingElement, childs);
            }
        }
    }
}
