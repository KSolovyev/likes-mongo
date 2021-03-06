import org.junit.Ignore;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

/**
 * Created by Solovyev on 09/10/2016.
 */
public class RepeatRunner extends BlockJUnit4ClassRunner {
    public RepeatRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    protected Description describeChild(FrameworkMethod method) {
        if(isIgnored(method) || !isRepeatable(method)) {
         return super.describeChild(method);
        }
        return describeRepeatTest(method);
    }

    private Description describeRepeatTest(FrameworkMethod method) {
        final int times = method.getAnnotation(Repeat.class).value();

        final Description description = Description.createSuiteDescription(
                testName(method) + " [" + times + " times]",
                method.getAnnotations());

        for (int i = 1; i <= times; i++) {
            description.addChild(Description.createTestDescription(
                    getTestClass().getJavaClass(),
                    "[" + i + "] " + testName(method)));
        }
        return description;
    }

    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier) {
        final Description descriptions = describeChild(method);
        if(isIgnored(method) || !isRepeatable(method)) {
            super.runChild(method, notifier);
            return;
        }
        for (Description description : descriptions.getChildren()) {
            runLeaf(methodBlock(method), description, notifier);
        }

    }

    private boolean isRepeatable(FrameworkMethod method) {
        return method.getAnnotation(Repeat.class) != null;
    }
}
