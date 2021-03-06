package net.foxopen.fox.command.builtin;

import net.foxopen.fox.ContextUElem;
import net.foxopen.fox.XFUtil;
import net.foxopen.fox.command.Command;
import net.foxopen.fox.command.CommandFactory;
import net.foxopen.fox.command.flow.XDoControlFlow;
import net.foxopen.fox.command.flow.XDoControlFlowContinue;
import net.foxopen.fox.dom.DOM;
import net.foxopen.fox.dom.xpath.XPathResult;
import net.foxopen.fox.ex.ExActionFailed;
import net.foxopen.fox.ex.ExDoSyntax;
import net.foxopen.fox.ex.ExInternal;
import net.foxopen.fox.module.Mod;
import net.foxopen.fox.thread.ActionRequestContext;
import net.foxopen.fox.track.Track;

import java.util.Collection;
import java.util.Collections;

/**
 * Simple command that logs some message.
 */
public class LogCommand
extends BuiltInCommand {

   /** The message to log. */
  private final String mMessage;
  private final String mXPath;

   /**
   * Constructs the command from the XML element specified.
   *
   * @param commandElement the element from which the command will
   *        be constructed.
   */
  private LogCommand(DOM commandElement) {
    super(commandElement);
    mMessage = commandElement.getAttr("message");
    mXPath = commandElement.getAttr("xpath");
  }

  public boolean isCallTransition() {
    return false;
  }

  @Override
  public XDoControlFlow run(ActionRequestContext pRequestContext) {

    ContextUElem lContextUElem = pRequestContext.getContextUElem();
    String lMessage;
    try {
      lMessage = lContextUElem.extendedStringOrXPathString(lContextUElem.attachDOM(), mMessage);
    }
    catch (ExActionFailed e) {
      throw new ExInternal("Message XPath error in fm:log command", e);
    }

    lMessage = XFUtil.nvl(lMessage, "[Null message]");

    XPathResult lXPathResult;
    if(!XFUtil.isNull(mXPath)) {
      try {
        lXPathResult = lContextUElem.extendedXPathResult(lContextUElem.attachDOM(), mXPath);
      }
      catch (ExActionFailed e) {
        throw new ExInternal("XPath error in fm:log command", e);
      }
    }
    else {
      lXPathResult = null;
    }

    Track.pushInfo("LogMessage", lMessage);
    try {
      if(lXPathResult != null) {
        StringBuffer lBuffer = new StringBuffer();
        lXPathResult.printResultAsXML(lBuffer);
        Track.logInfoXMLString("XPathResult", lBuffer.toString());
      }
    }
    finally {
      Track.pop("LogMessage");
    }

    return XDoControlFlowContinue.instance();
  }

  public static class Factory
  implements CommandFactory {

    @Override
    public Command create(Mod pModule, DOM pMarkupDOM) throws ExDoSyntax {
      return new LogCommand(pMarkupDOM);
    }

    @Override
    public Collection<String> getCommandElementNames() {
      return Collections.singleton("log");
    }
  }
}
