/* Copyright 2012 predic8 GmbH, www.predic8.com

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License. */

package com.predic8.schema

import com.predic8.util.*
import com.predic8.xml.util.*
import javax.xml.stream.*
import com.predic8.soamodel.*
import org.apache.commons.logging.*
import javax.xml.namespace.QName as JQName
import static com.predic8.soamodel.Consts.SCHEMA_NS

class Include extends SchemaComponent {
  
  private Log log = LogFactory.getLog(this.class)
  
  String schemaLocation

   protected parseAttributes(token, ctx){
    schemaLocation = token.getAttributeValue( null , 'schemaLocation')
    if(schema.includedPaths.contains(HTTPUtil.getLocation(schema.baseDir,schemaLocation))) return
    parseIncludedSchema(ctx)
  }

  private parseIncludedSchema(ctx){
    def resource = schema.resourceResolver.resolve(this, schema.baseDir)
  	
    def incToken = XMLInputFactory.newInstance().createXMLStreamReader(resource)
    while(incToken.hasNext()) {
      if(incToken.startElement) {
        if(incToken.name.getLocalPart() =='schema'){
          break
        }
      }
      if(incToken.hasNext()) incToken.next()
    }
    def origBaseDir = schema.baseDir
    schema.baseDir = HTTPUtil.updateBaseDir(schemaLocation , schema.baseDir)
    log.debug("includedSchema.baseDir ${schema.baseDir} ")
    schema.parse(incToken, new SchemaParserContext(targetNamespace:schema.targetNamespace, importedSchemas:ctx.importedSchemas, errors: ctx.errors))
    schema.baseDir = origBaseDir

    schema.includedPaths << HTTPUtil.getLocation(schema.baseDir,schemaLocation)
  }

  protected getElementName(){
    new JQName(SCHEMA_NS, 'include')
  }

  String toString(){
    "schemaLocation=$schemaLocation"
  }
  
}
