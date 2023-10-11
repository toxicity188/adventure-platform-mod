/*
 * This file is part of adventure-platform-fabric, licensed under the MIT License.
 *
 * Copyright (c) 2020-2023 KyoriPowered
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
package net.kyori.adventure.platform.fabric.impl.mixin.minecraft.network.chat;

import com.google.gson.JsonElement;
import net.kyori.adventure.platform.fabric.impl.NonWrappingComponentSerializer;
import net.kyori.adventure.platform.fabric.impl.WrappedComponent;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Component.class)
public interface ComponentMixin extends ComponentLike {
  @Override
  default net.kyori.adventure.text.@NotNull Component asComponent() {
    return NonWrappingComponentSerializer.INSTANCE.deserialize((Component) this);
  }

  // Hook into the codec?

  @Mixin(Component.Serializer.class)
  abstract class SerializerMixin {
    @Inject(method = "serialize(Lnet/minecraft/network/chat/Component;)Lcom/google/gson/JsonElement;", at = @At("HEAD"), cancellable = true)
    private static void adventure$writeComponentBase(final Component text, final CallbackInfoReturnable<JsonElement> cir) {
      // Skip serialization logic if we're a top-level our codec
      if (text instanceof WrappedComponent w) {
        final @Nullable Component converted = w.deepConvertedIfPresent();
        if (converted == null) {
          cir.setReturnValue(GsonComponentSerializer.gson().serializeToTree(w.wrapped()));
        }
      }
    }
  }
}
