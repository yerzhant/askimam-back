package kz.azan.askimam

import com.tngtech.archunit.base.DescribedPredicate
import com.tngtech.archunit.core.domain.JavaMethod
import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeJars
import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.ArchCondition
import com.tngtech.archunit.lang.ConditionEvents
import com.tngtech.archunit.lang.SimpleConditionEvent
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods
import com.tngtech.archunit.library.Architectures
import kz.azan.askimam.common.arch.PackagePrivate

@Suppress("PropertyName", "HasPlatformType")
@AnalyzeClasses(
    packagesOf = [AskimamApplication::class],
    importOptions = [DoNotIncludeTests::class, DoNotIncludeJars::class]
)
class ArchitectureTests {

    private val domain = "Domain"
    private val application = "Application"
//    private val web = "Web"
//    private val infra = "Infra"

    private val areAnnotatedByAPackagePrivate =
        object : DescribedPredicate<JavaMethod>("are annotated as a package private") {
            override fun apply(input: JavaMethod?): Boolean {
                return input?.isAnnotatedWith(PackagePrivate::class.java)!!
            }
        }

    private val notBeCalledFromExternalPackages =
        object : ArchCondition<JavaMethod>("not be called from outside of their packages") {
            override fun check(item: JavaMethod?, events: ConditionEvents?) {
                item?.callsOfSelf?.filter {
                    it.targetOwner.`package` != it.originOwner.`package`
                }?.forEach {
                    events?.add(SimpleConditionEvent.violated(it, "${it.originOwner.name}, line ${it.lineNumber}"))
                }
            }
        }

    @ArchTest
    val `entity package private members should not be accessed from outside of their packages` =
        methods().that(areAnnotatedByAPackagePrivate).should(notBeCalledFromExternalPackages)

    @ArchTest
    val layerRule = Architectures.layeredArchitecture()
        .layer(domain).definedBy("..domain..")
        .layer(application).definedBy("..app..")
//        .layer(web).definedBy("..web..")
//        .layer(infra).definedBy("..infra..")
//        .whereLayer(application).mayOnlyBeAccessedByLayers(web)
//        .whereLayer(web).mayNotBeAccessedByAnyLayer()
//        .whereLayer(infra).mayNotBeAccessedByAnyLayer()
}