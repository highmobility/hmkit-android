package com.high_mobility.hmkit.Command;

/**
 * Created by ttiganik on 07/06/16.
 */
public class CommandParseException extends Exception{
    public enum CommandExceptionCode {
        // the command bytes could not be parsed
        PARSE_ERROR
    }

    public CommandExceptionCode code;

    public CommandParseException() {
        this.code = CommandExceptionCode.PARSE_ERROR;
    }
}