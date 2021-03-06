/*
 * #!
 * Ontopoly Editor
 * #-
 * Copyright (C) 2001 - 2013 The Ontopia Project
 * #-
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * !#
 */
package ontopoly.components;


import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.model.IModel;

/**
 * Subclass of Radio that notifies an AjaxParentFormChoiceComponentUpdatingBehavior when 
 * it is being rendered. This is useful when the Radio is rendered via an AJAX call.
 * @author grove
 * @see AjaxParentFormChoiceComponentUpdatingBehavior
 */
public class AjaxParentRadioChild<T> extends Radio<T> implements IHeaderContributor {

  private AjaxParentFormChoiceComponentUpdatingBehavior apfc;
  
  public AjaxParentRadioChild(String id, IModel<T> model, AjaxParentFormChoiceComponentUpdatingBehavior apfc) {
    super(id, model);
    setOutputMarkupId(true);
    this.apfc = apfc;
  }
  @Override
  public void renderHead(IHeaderResponse response) {    
    response.renderOnLoadJavascript("attachChoiceHandler('" + getMarkupId() +
        "', function() {" + apfc.getCallbackFunction() + "});");    
  }

}
