/*
 * (C) Copyright 2006-2008 Nuxeo SAS (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     bstefanescu
 *
 * $Id$
 */

package org.nuxeo.chemistry.shell.cmds;

import java.io.File;
import java.util.Stack;

import org.nuxeo.chemistry.shell.Console;
import org.nuxeo.chemistry.shell.command.AnnotatedCommand;
import org.nuxeo.chemistry.shell.command.Cmd;
import org.nuxeo.chemistry.shell.command.CommandLine;
import org.nuxeo.chemistry.shell.context.Application;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
@Cmd(syntax="lpopd", synopsis="Pop local directory stack")
@SuppressWarnings("unchecked")
public class LPopd extends AnnotatedCommand {

    public final static String WDIR_STACK_KEY = "wdir.stack";
    @Override
    public void run(Application app, CommandLine cmdLine) throws Exception {
        Stack<File> stack = (Stack<File>)app.getData(WDIR_STACK_KEY);
        if (stack == null) {
            Console.getDefault().warn("No more directories on the stack");
            return;
        }
        File file = stack.pop();
        if (stack.isEmpty()) {
            app.setData(WDIR_STACK_KEY, null);
        }
        app.setWorkingDirectory(file);
        Console.getDefault().updatePrompt();
    }

}
