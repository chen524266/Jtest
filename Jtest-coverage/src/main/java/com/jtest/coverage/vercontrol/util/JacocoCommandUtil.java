package com.jtest.coverage.vercontrol.util;

import org.jacoco.cli.internal.Main;

import java.io.PrintWriter;
import java.io.StringWriter;

public class JacocoCommandUtil extends Main {

    static StringWriter out;
    static StringWriter err;
    static int result;

    static {
        out = new StringWriter();
        err = new StringWriter();
    }

    public static int execute(String... args) throws Exception {
        result = new Main(args).execute(new PrintWriter(out),
                new PrintWriter(err));
        return result;
    }
}
