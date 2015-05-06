/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered.org <http://www.spongepowered.org>
 * Copyright (c) contributors
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
 */
package org.spongepowered.asm.mixin.injection.callback;

import org.spongepowered.asm.lib.Type;

/**
 * CallbackInfo instances are passed to callbacks in order to provide
 * information and handling opportunities to the callback to interact with the
 * callback itself. For example by allowing the callback to be "cancelled" and
 * return from a method prematurely. 
 */
public class CallbackInfo implements Cancellable {

    protected static final String STRING = "Ljava/lang/String;";
    protected static final String OBJECT = "Ljava/lang/Object;";

    /**
     * Method name being injected into, this is useful if a single callback is
     * injecting into multiple methods.
     */
    private final String name;

    /**
     * True if this callback is cancellable
     */
    private final boolean cancellable;

    /**
     * True if this callback has been cancelled
     */
    private boolean cancelled;

    /**
     * This ctor is always called by injected code
     * 
     * @param name calling method name
     * @param cancellable true if the callback can be cancelled
     */
    public CallbackInfo(String name, boolean cancellable) {
        this.name = name;
        this.cancellable = cancellable;
    }

    /**
     * Get the method name where this callback originated
     * 
     * @return the name of the calling method
     */
    public String getName() {
        return this.name;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("CallbackInfo[TYPE=%s,NAME=%s,CANCELLABLE=%s]", this.getClass().getSimpleName(), this.name, this.cancellable);
    }

    @Override
    public final boolean isCancellable() {
        return this.cancellable;
    }

    @Override
    public final boolean isCancelled() {
        return this.cancelled;
    }

    /* (non-Javadoc)
     * @see org.spongepowered.asm.mixin.injection.callback.Cancellable#cancel()
     */
    @Override
    public void cancel() throws CancellationException {
        if (!this.cancellable) {
            throw new CancellationException(String.format("The call %s is not cancellable.", this.name));
        }

        this.cancelled = true;
    }
    
    // Methods below this point used by the CallbackInjector

    static String getCallInfoClassName() {
        return CallbackInfo.class.getName();
    }

    public static String getCallInfoClassName(Type returnType) {
        return (returnType.equals(Type.VOID_TYPE) ? CallbackInfo.class.getName() : CallbackInfoReturnable.class.getName()).replace('.', '/');
    }

    static String getConstructorDescriptor(Type returnType) {
        if (returnType.equals(Type.VOID_TYPE)) {
            return CallbackInfo.getConstructorDescriptor();
        }

        if (returnType.getSort() == Type.OBJECT) {
            return String.format("(%sZ%s)V", CallbackInfo.STRING, CallbackInfo.OBJECT);
        }

        return String.format("(%sZ%s)V", CallbackInfo.STRING, returnType.getDescriptor());
    }

    static String getConstructorDescriptor() {
        return String.format("(%sZ)V", CallbackInfo.STRING);
    }

    static String getIsCancelledMethodName() {
        return "isCancelled";
    }

    static String getIsCancelledMethodSig() {
        return "()Z";
    }
}
