package com.github.simpleexpress.page;


public class InvalidEmailException extends PageException
{
    public InvalidEmailException(String email)
    {
        super(email);
    }
}
