package com.trsvax.bootstrap.mixins;

import org.apache.tapestry5.ClientElement;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.annotations.AfterRender;
import org.apache.tapestry5.annotations.BeginRender;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.MixinAfter;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.corelib.components.Grid;
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.dom.Visitor;
import org.apache.tapestry5.internal.services.ArrayEventContext;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.ContextPathEncoder;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.got5.tapestry5.jquery.ImportJQueryUI;


@ImportJQueryUI(value = {"jquery.ui.widget", "jquery.ui.mouse", "jquery.ui.droppable"})
@Import(library = { "classpath:com/trsvax/bootstrap/assets/mixins/droppable/droppable.js" })
@MixinAfter
public class Droppable {	
	@Parameter
	Object[] context;
	@Parameter(defaultPrefix="literal")
	String event;
	@Parameter(defaultPrefix="literal")
	String zoneSelector;
	@Parameter(defaultPrefix="literal")
	JSONObject params;
	@Parameter
	boolean disabled;

    @Inject
    TypeCoercer typeCoercer;
    @Inject
    ContextPathEncoder contextPathEncoder;
	@Inject
	JavaScriptSupport javaScriptSupport;
	
	@Parameter
	private String elementName;
	
	@Inject
	ComponentResources resources;
	
	@Inject
	Request request;
	
	private Element element;
	
	private JSONObject spec;
	
	@BeginRender
	void beginRender() {
        if (disabled) {
            return;
        }
        if ( event == null ) {
			event = "drop";
		}
		String link = resources.getContainerResources().createEventLink(event).toAbsoluteURI();
		if ( params == null ) {
			params = new JSONObject();
		}
		//spec.put("disabled",false);
		//spec.put("accept", "*");
        putConditionally("activeClass", "ui-state-default");
        //spec.put("addClasses",true);
		//spec.put("greedy",false);
        putConditionally("hoverClass", "ui-state-hover");
        //spec.put("scope","default");
		//spec.put("tolerance","intersect");
		
		spec = new JSONObject();

		if ( zoneSelector != null ) {
			spec.put("zoneSelector", zoneSelector);
		}
		spec.put("params", params);
		spec.put("BaseURL",link);
        if (!spec.has("context")) {
            ArrayEventContext aec = new ArrayEventContext(typeCoercer, defaulted(context));
            spec.put("context", String.format("/%s", contextPathEncoder.encodeIntoPath(aec)));
        }
    }

    private void putConditionally(String key, Object value) {
        if (!params.has(key)) {
            params.put(key, value);
        }
    }

	@AfterRender
	public void afterRender(MarkupWriter writer) {
        if (disabled) {
            return;
        }
		String id = null;
		if ( elementName == null ) {
			elementName = resources.getElementName();
		}
		if ( event == null ) {
			event = "drop";
		}
		Object compoment =  resources.getContainer();
		if ( ClientElement.class.isAssignableFrom(compoment.getClass()) ) {
			id = ((ClientElement)compoment).getClientId();
		} else {
			id = javaScriptSupport.allocateClientId(resources);
		}
		if ( Grid.class.isAssignableFrom(compoment.getClass()) ) {
			elementName = "tbody";
		}
		
		element = writer.getElement();
		
		if ( elementName != null ) {
			element.visit( new Visitor() {
				boolean first = true;
				public void visit(Element e) {
					if ( e.getName().equals(elementName)) {
						if ( first ) {
							first = false;
							element = e;
						}
					}
					if ( e.getName().equals("tr"))  {
						String c = e.getAttribute("class");
						if ( c != null ) {
							e.forceAttributes("id",c.split(" ")[0]);
						}
					}
					
				}
			});
		}
		if ( element != null ) {
			String currentID = element.getAttribute("id");
			if ( currentID != null ) {
				id = currentID;
			} else {
				element.forceAttributes("id",id);
			}	
		}
		if ( ! spec.has("selector")) {
			spec.put("selector", "#"+id);
		}

		if ( !(request.isXHR() && zoneSelector != null) ) {
			javaScriptSupport.addInitializerCall("jqDroppable", spec);
		}
		
	}

    private Object[] defaulted(Object[] context) {
        return context == null ? new String[0] : context;
    }

	public JSONObject getSpec() {
		return spec;
	}

}
