/*
 * This file is part of adventure-platform-mod, licensed under the MIT License.
 *
 * Copyright (c) 2020-2024 KyoriPowered
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
package net.kyori.adventure.platform.modcommon.impl.client;

import java.util.function.Function;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.modcommon.MinecraftClientAudiences;
import net.kyori.adventure.platform.modcommon.impl.AdventureCommon;
import net.kyori.adventure.platform.modcommon.impl.MinecraftAudiencesInternal;
import net.kyori.adventure.platform.modcommon.impl.NonWrappingComponentSerializer;
import net.kyori.adventure.pointer.Pointered;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.text.renderer.ComponentRenderer;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.server.RegistryLayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static java.util.Objects.requireNonNull;

public class MinecraftClientAudiencesImpl implements MinecraftClientAudiences, MinecraftAudiencesInternal {
  public static final MinecraftClientAudiences INSTANCE = new Builder().build();
  private final Function<Pointered, ?> partition;
  private final ComponentRenderer<Pointered> renderer;
  private final ClientAudience audience;

  public MinecraftClientAudiencesImpl(final Function<Pointered, ?> partition, final ComponentRenderer<Pointered> renderer) {
    this.partition = partition;
    this.renderer = renderer;
    this.audience = new ClientAudience(Minecraft.getInstance(), this);
  }

  @Override
  public @NotNull Audience audience() {
    return this.audience;
  }

  @Override
  public @NotNull ComponentSerializer<net.kyori.adventure.text.Component, net.kyori.adventure.text.Component, Component> nonWrappingSerializer() {
    return NonWrappingComponentSerializer.INSTANCE;
  }

  @Override
  public @NotNull ComponentFlattener flattener() {
    return AdventureCommon.FLATTENER;
  }

  @Override
  public @NotNull ComponentRenderer<Pointered> renderer() {
    return this.renderer;
  }

  @Override
  public Component asNative(final net.kyori.adventure.text.Component adventure) {
    if (adventure == null) {
      return null;
    }
    return new ClientWrappedComponent(requireNonNull(adventure, "adventure"), this.partition, this.renderer);
  }

  public Function<Pointered, ?> partition() {
    return this.partition;
  }

  @Override
  public @NotNull RegistryAccess registryAccess() {
    final @Nullable ClientLevel level = Minecraft.getInstance().level;
    return level == null ? RegistryLayer.createRegistryAccess().compositeAccess() : level.registryAccess();
  }

  public static final class Builder implements MinecraftClientAudiences.Builder {
    private Function<Pointered, ?> partition;
    private ComponentRenderer<Pointered> renderer;

    public Builder() {
      this.componentRenderer(AdventureCommon.localePartition(), GlobalTranslator.renderer());
    }

    @Override
    public MinecraftClientAudiences.@NotNull Builder componentRenderer(final @NotNull ComponentRenderer<Pointered> componentRenderer) {
      this.renderer = requireNonNull(componentRenderer, "componentRenderer");
      return this;
    }

    @Override
    public MinecraftClientAudiences.@NotNull Builder partition(final @NotNull Function<Pointered, ?> partitionFunction) {
      this.partition = requireNonNull(partitionFunction, "partitionFunction");
      return this;
    }

    @Override
    public @NotNull MinecraftClientAudiences build() {
      return new MinecraftClientAudiencesImpl(this.partition, this.renderer);
    }
  }
}
