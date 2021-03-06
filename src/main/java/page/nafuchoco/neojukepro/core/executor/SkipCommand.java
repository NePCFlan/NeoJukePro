/*
 * Copyright 2020 NAFU_at.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package page.nafuchoco.neojukepro.core.executor;

import page.nafuchoco.neojukepro.core.Main;
import page.nafuchoco.neojukepro.core.MessageManager;
import page.nafuchoco.neojukepro.core.NeoJukeLauncher;
import page.nafuchoco.neojukepro.core.command.CommandContext;
import page.nafuchoco.neojukepro.core.command.CommandExecutor;
import page.nafuchoco.neojukepro.core.command.MessageUtil;
import page.nafuchoco.neojukepro.core.player.GuildAudioPlayer;
import page.nafuchoco.neojukepro.core.player.GuildTrackContext;

public class SkipCommand extends CommandExecutor {
    private static final NeoJukeLauncher launcher = Main.getLauncher();

    public SkipCommand(String name, String... aliases) {
        super(name, aliases);
    }

    @Override
    public void onInvoke(CommandContext context) {
        GuildAudioPlayer audioPlayer = launcher.getPlayerRegistry().getGuildAudioPlayer(context.getGuild());
        if (audioPlayer.getNowPlaying() != null) {
            if (!context.getMessage().getMentionedMembers().isEmpty()) {
                int skipcount =
                        context.getMessage().getMentionedMembers().stream().mapToInt(member -> audioPlayer.skip(member).size()).sum();
                context.getChannel().sendMessage(MessageUtil.format(MessageManager.getMessage("command.skip.skip.count"), skipcount)).queue();
            } else if (context.getArgs().length >= 1) {
                String indexS = context.getArgs()[0];
                if (indexS.contains("-")) {
                    String[] split = indexS.split("-");
                    if (split.length == 1 && indexS.endsWith("-")) {
                        int below = Integer.parseInt(indexS.replace("-", ""));
                        audioPlayer.skip(below);
                        context.getChannel().sendMessage(MessageUtil.format(MessageManager.getMessage("command.skip.below"), below)).queue();
                    } else {
                        audioPlayer.skip(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
                        context.getChannel().sendMessage(MessageUtil.format(MessageManager.getMessage("command.skip.between"), split[0], split[1])).queue();
                    }
                } else {
                    try {
                        context.getChannel().sendMessage(MessageUtil.format(MessageManager.getMessage("command.skip"),
                                audioPlayer.skip(Integer.parseInt(indexS), Integer.parseInt(indexS)).get(0).getTrack().getInfo().title)).queue();
                    } catch (IllegalArgumentException e) {
                        // nothing
                    }
                }
            } else {
                GuildTrackContext nowPlaying = audioPlayer.getNowPlaying();
                audioPlayer.skip();
                context.getChannel().sendMessage(MessageUtil.format(MessageManager.getMessage("command.skip"),
                        nowPlaying.getTrack().getInfo().title)).queue();
            }
        }
    }

    @Override
    public String getDescription() {
        return "Skip the track.";
    }

    @Override
    public String getHelp() {
        return getName() + "<args>\n----\n" +
                "<from-to>: Skips tracks between the specified numbers." +
                "<below->: Skips tracks after the specified number." +
                "<invoker>: Skips the tracks added by the specified user.";
    }

    @Override
    public int getRequiredPerm() {
        return 0;
    }
}
