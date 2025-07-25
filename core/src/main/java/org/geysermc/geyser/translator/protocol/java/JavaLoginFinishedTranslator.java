/*
 * Copyright (c) 2019-2022 GeyserMC. http://geysermc.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * @author GeyserMC
 * @link https://github.com/GeyserMC/Geyser
 */

package org.geysermc.geyser.translator.protocol.java;

import net.kyori.adventure.key.Key;
import org.geysermc.geyser.api.network.AuthType;
import org.geysermc.geyser.entity.type.player.PlayerEntity;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.skin.SkinManager;
import org.geysermc.geyser.translator.protocol.PacketTranslator;
import org.geysermc.geyser.translator.protocol.Translator;
import org.geysermc.geyser.util.PluginMessageUtils;
import org.geysermc.mcprotocollib.auth.GameProfile;
import org.geysermc.mcprotocollib.protocol.data.ProtocolState;
import org.geysermc.mcprotocollib.protocol.packet.common.serverbound.ServerboundCustomPayloadPacket;
import org.geysermc.mcprotocollib.protocol.packet.login.clientbound.ClientboundLoginFinishedPacket;

/**
 * Triggers protocol change LOGIN -> CONFIGURATION
 */
@Translator(packet = ClientboundLoginFinishedPacket.class)
public class JavaLoginFinishedTranslator extends PacketTranslator<ClientboundLoginFinishedPacket> {

    @Override
    public void translate(GeyserSession session, ClientboundLoginFinishedPacket packet) {
        PlayerEntity playerEntity = session.getPlayerEntity();
        AuthType remoteAuthType = session.remoteServer().authType();

        // Required, or else Floodgate players break with Spigot chunk caching
        GameProfile profile = packet.getProfile();
        playerEntity.setUsername(profile.getName());
        playerEntity.setUuid(profile.getId());

        session.getGeyser().getSessionManager().addSession(playerEntity.getUuid(), session);

        // We no longer need these variables; they're just taking up space in memory now
        session.setCertChainData(null);
        session.getClientData().setOriginalString(null);

        // configuration phase stuff that the vanilla client replies with after receiving the GameProfilePacket
        session.sendDownstreamPacket(new ServerboundCustomPayloadPacket(Key.key("brand"), PluginMessageUtils.getGeyserBrandData()), ProtocolState.CONFIGURATION);
        session.sendJavaClientSettings();
    }
}
