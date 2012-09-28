package com.cueup.hegemon.annotations;
/*
 * Copyright 2012 Greplin, Inc. All Rights Reserved.
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Annotates methods used only by JavaScript.
 */
@Target({ ElementType.METHOD, ElementType.TYPE, ElementType.FIELD, ElementType.CONSTRUCTOR })
public @interface ReferencedByJavascript {
}
