package net.blockhost.commons.core.io;

import java.io.IOException;

/// A functional interface like [java.util.function.Consumer] that allows checked [IOException]s.
///
/// This is useful for passing file/stream write operations as lambdas without
/// requiring callers to wrap the body in a try-catch.
///
/// @param <T> the type of the input to the operation
@FunctionalInterface
public interface IOConsumer<T> {

    /// Performs this operation on the given argument.
    ///
    /// @param t the input argument
    /// @throws IOException if an I/O error occurs
    void accept(T t) throws IOException;
}
