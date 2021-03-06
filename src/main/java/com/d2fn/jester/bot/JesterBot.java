package com.d2fn.jester.bot;

import com.d2fn.jester.config.BotConfiguration;
import com.d2fn.jester.config.ChannelConfiguration;
import com.d2fn.jester.plugin.Message;
import com.d2fn.jester.plugin.Plugin;
import com.yammer.dropwizard.lifecycle.Managed;
import org.jibble.pircbot.PircBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * JesterBot
 * @author Dietrich Featherston
 */
public class JesterBot extends PircBot implements Managed {
    private static final Logger log = LoggerFactory.getLogger(JesterBot.class);

    private BotConfiguration config;
    private Collection<Plugin> plugins;
    
    public JesterBot(BotConfiguration config, Collection<Plugin> plugins) {
        super.setName(config.getName());
        this.config = config;
        this.plugins = plugins;
    }

    @Override
    protected void onMessage(String channel, String sender, String login, String hostname, String message) {

        super.onMessage(channel, sender, login, hostname, message);
        log.debug("message( channel='{}', sender='{}', login='{}', hostname='{}', message='{}'", channel, sender, login, hostname, message);

        if(sender.equals(getName())) {
            // don't process messages that we sent
            return;
        }
        
        // remit messages to all plugins
        Message m = new Message(channel, sender, login, hostname, message);
        sendMessageToPlugins(m);
    }

    private void sendMessageToPlugins(Message m) {
        for(Plugin plugin : plugins) {
            try {
                plugin.call(this, m);
            }
            catch(Exception e) {
                log.error("error encountered running plugin {}", plugin.getName(), e);
            }
        }
    }

    @Override
    protected void onDisconnect() {
        super.onDisconnect();
        log.info("disconnected, retrying in 5s ...");
        try {
            Thread.sleep(5000L);
            start();
        } catch (Exception e) {
            log.error("interrupted during sleep", e);
        }
        
    }

    public void start() throws Exception {
        log.info("starting bot {}", config.getName());
        log.info(" - connecting to {}", config.getHostname());
        connect(config.getHostname());
        setVerbose(config.verbose());
        for(ChannelConfiguration channel : config.getChannels()) {
            joinChannel(channel);
        }
    }

    public void stop() throws Exception {
        log.info("stopping bot {}", config.getName());
        disconnect();
    }

    private void joinChannel(ChannelConfiguration channel) {
        if(channel.isProtected()) {
            joinChannel(channel.getName(), channel.getKey());
        }
        else {
            joinChannel(channel.getName());
        }
    }
}
