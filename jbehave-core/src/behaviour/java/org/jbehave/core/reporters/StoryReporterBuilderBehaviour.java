package org.jbehave.core.reporters;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.jbehave.Ensure.ensureThat;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.STATS;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.TXT;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.jbehave.core.JUnitStory;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.parser.StoryPathResolver;
import org.jbehave.core.parser.UnderscoredCamelCaseResolver;
import org.jbehave.core.reporters.FilePrintStreamFactory.FileConfiguration;
import org.jbehave.core.reporters.StoryReporterBuilder.Format;
import org.junit.Test;

public class StoryReporterBuilderBehaviour {


    @Test
    public void shouldBuildWithStatsByDefault() throws IOException {
        FilePrintStreamFactory factory = filePrintSteamFactoryFor(MyStory.class);
        StoryReporterBuilder builder = new StoryReporterBuilder(factory);

        // When
        StoryReporter reporter = builder.build();
        
        // Then
        ensureThat(reporter instanceof DelegatingStoryReporter);
        Map<Format, StoryReporter> delegates = builder.getDelegates();
        ensureThat(delegates.size(), equalTo(1));
        ensureThat(delegates.get(STATS) instanceof StatisticsStoryReporter);
    }

    @Test
    public void shouldAllowOverrideOfDefaultFileDirectory() throws IOException {

        FilePrintStreamFactory factory = filePrintSteamFactoryFor(MyStory.class);
        StoryReporterBuilder builder = new StoryReporterBuilder(factory);

        // When
        String fileDirectory = "my-reports";
        builder.outputTo(fileDirectory);
        
        // Then
        ensureThat(builder.fileConfiguration("").getDirectory().endsWith(fileDirectory));
    }

    @Test
    public void shouldBuildAndOverrideDefaultReporterForAGivenFormat() throws IOException {
        FilePrintStreamFactory factory = filePrintSteamFactoryFor(MyStory.class);
        final StoryReporter txtReporter = new PrintStreamStoryReporter(factory.createPrintStream(), new Properties(),  new LocalizedKeywords(), true);
        StoryReporterBuilder builder = new StoryReporterBuilder(factory){
               public StoryReporter reporterFor(Format format){
                       switch (format) {
                           case TXT:
                               factory.useConfiguration(new FileConfiguration("text"));
                               return txtReporter;
                            default:
                               return super.reporterFor(format);
                       }
                   }
        };
        
        // When
        StoryReporter reporter = builder.with(TXT).build();
        
        // Then
        ensureThat(reporter instanceof DelegatingStoryReporter);
        Map<Format, StoryReporter> delegates = builder.getDelegates();
        ensureThat(delegates.size(), equalTo(2));
        ensureThat(delegates.get(TXT), equalTo(txtReporter));
    }

    private FilePrintStreamFactory filePrintSteamFactoryFor(Class<MyStory> storyClass) {
        StoryPathResolver resolver = new UnderscoredCamelCaseResolver(".story");
        String storyPath = resolver.resolve(storyClass);
        return new FilePrintStreamFactory(storyPath);
    }


    private static class MyStory extends JUnitStory {

    }
}
