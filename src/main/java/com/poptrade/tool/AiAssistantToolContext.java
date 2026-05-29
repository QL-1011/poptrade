package com.poptrade.tool;

/**
 * AI tool invocation context.
 * Tools must use the authenticated backend user, never a user id supplied by the model.
 */
public final class AiAssistantToolContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();

    private AiAssistantToolContext() {
    }

    public static Scope open(Long userId) {
        USER_ID.set(userId);
        return new Scope();
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

    public static void clear() {
        USER_ID.remove();
    }

    public static final class Scope implements AutoCloseable {
        private Scope() {
        }

        @Override
        public void close() {
            AiAssistantToolContext.clear();
        }
    }
}
