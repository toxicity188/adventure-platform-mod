/*
 * This file is part of adventure-platform-fabric, licensed under the MIT License.
 *
 * Copyright (c) 2020-2022 KyoriPowered
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.kyori.adventure.platform.fabric.impl.mixin.minecraft.server.rcon;

import com.google.common.collect.MapMaker;
import java.util.Map;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.permission.PermissionChecker;
import net.kyori.adventure.platform.fabric.FabricServerAudiences;
import net.kyori.adventure.platform.fabric.impl.FabricAudiencesInternal;
import net.kyori.adventure.platform.fabric.impl.server.FabricServerAudiencesImpl;
import net.kyori.adventure.platform.fabric.impl.server.PlainAudience;
import net.kyori.adventure.platform.fabric.impl.server.RenderableAudience;
import net.kyori.adventure.pointer.Pointers;
import net.kyori.adventure.util.TriState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.rcon.RconConsoleSource;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(RconConsoleSource.class)
public abstract class RconConsoleSourceMixin implements RenderableAudience, ForwardingAudience.Single {
  // @formatter:off
  @Shadow @Final private StringBuffer buffer;
  @Shadow @Final private MinecraftServer server;
  // @formatter:off

  private volatile Pointers adventure$pointers;
  private final Map<FabricAudiencesInternal, Audience> adventure$renderers = new MapMaker().weakKeys().makeMap();

  @Override
  public Audience renderUsing(final FabricServerAudiencesImpl controller) {
    return this.adventure$renderers.computeIfAbsent(controller, ctrl -> new PlainAudience(ctrl, this, this.buffer::append));
  }

  @Override
  public @NotNull Audience audience() {
    return FabricServerAudiences.of(this.server).audience((RconConsoleSource) (Object) this);
  }

  @Override
  public @NotNull Pointers pointers() {
    if (this.adventure$pointers == null) {
      synchronized (this) {
        if (this.adventure$pointers == null) {
          return this.adventure$pointers = Pointers.builder()
            .withStatic(Identity.NAME, "Server")
            .withStatic(PermissionChecker.POINTER, perm -> TriState.TRUE)
            .build();
        }
      }
    }
    return this.adventure$pointers;
  }
}
