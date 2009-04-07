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

import org.nuxeo.chemistry.shell.Console;
import org.nuxeo.chemistry.shell.command.AnnotatedCommand;
import org.nuxeo.chemistry.shell.command.Cmd;
import org.nuxeo.chemistry.shell.command.CommandLine;
import org.nuxeo.chemistry.shell.command.CommandParameter;
import org.nuxeo.chemistry.shell.context.Application;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
@Cmd(syntax="lcd [dir:dir]", synopsis="Change local working directory")
public class LCd extends AnnotatedCommand {
    
    
    @Override
    public void run(Application app, CommandLine cmdLine) throws Exception {
        CommandParameter param = cmdLine.getLastParameter();
        if (param != null) {
            String path = param.getValue();
            File file = app.getFile(path);
            if (file.isDirectory()) {
                app.setWorkingDirectory(file);
            } else {
                Console.getDefault().error("File "+file+" is not a directory");   
            }
            Console.getDefault().updatePrompt();
        }
    }

}
