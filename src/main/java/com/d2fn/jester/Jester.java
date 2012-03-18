package com.d2fn.jester;

import com.d2fn.jester.bot.JesterBot;
import com.d2fn.jester.config.JesterConfiguration;
import com.d2fn.jester.plugin.Plugin;
import com.d2fn.jester.plugin.WhoAreYouPlugin;
import com.d2fn.jester.plugin.gis.GoogleImageSearchPlugin;
import com.d2fn.jester.plugin.twitter.TwitterPlugin;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.client.HttpClientFactory;
import com.yammer.dropwizard.client.JerseyClient;
import com.yammer.dropwizard.client.JerseyClientFactory;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.config.HttpConfiguration;
import com.yammer.dropwizard.logging.Log;
import org.apache.http.client.HttpClient;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Jester
 * @author Dietrich Featherston
 */
public class Jester extends Service<JesterConfiguration> {
    
    private static final Log log = Log.forClass(Jester.class);
    
    public static void main(String[] args) throws Exception {
        new Jester().run(args);
    }

    private Jester() {
        super("jester");
    }

    @Override
    public void initialize(JesterConfiguration config, Environment environment) {
        log.info("\n\n{}\n", config.getProse());

//        JerseyClient httpClient = new JerseyClientFactory(config.getJerseyClientConfiguration()).build(environment);
        HttpClient httpClient = new HttpClientFactory(config.getHttpClientConfiguration()).build();

        Collection<Plugin> plugins = new ArrayList<Plugin>();
        plugins.add(new WhoAreYouPlugin());
        plugins.add(new GoogleImageSearchPlugin(httpClient));
        plugins.add(new TwitterPlugin(httpClient));
        // todo - add more plugins

        JesterBot bot = new JesterBot(config.getBot(), plugins);
        environment.manage(bot);
    }
}