/*
 * Copyright (c) 2015 Raytheon BBN Technologies Corp
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.poi.xdgf.usermodel;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.POIXMLException;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.util.Internal;
import org.apache.poi.xdgf.exceptions.XDGFException;
import org.apache.poi.xdgf.xml.XDGFXMLDocumentPart;
import org.apache.xmlbeans.XmlException;

import com.microsoft.schemas.office.visio.x2012.main.MasterType;
import com.microsoft.schemas.office.visio.x2012.main.MastersDocument;
import com.microsoft.schemas.office.visio.x2012.main.MastersType;

public class XDGFMasters extends XDGFXMLDocumentPart {

	MastersType _mastersObject;
	
	// key: id of master
	Map<Long, XDGFMaster> _masters = new HashMap<>();
	
	public XDGFMasters(PackagePart part, PackageRelationship rel, XDGFDocument document) {
		super(part, rel, document);
	}
	
	@Internal
	MastersType getXmlObject() {
		return _mastersObject;
	}
	
	@Override
	protected void onDocumentRead() {
		try {
			try {
				_mastersObject = MastersDocument.Factory.parse(getPackagePart().getInputStream()).getMasters();
			} catch (XmlException | IOException e) {
				throw new POIXMLException(e);
			}
			
			Map<String, MasterType> masterSettings = new HashMap<>();
			for (MasterType master: _mastersObject.getMasterArray()) {
				masterSettings.put(master.getRel().getId(), master);
			}
			
			// create the masters
			for (POIXMLDocumentPart part: getRelations()) {
				
				String relId = part.getPackageRelationship().getId();
				MasterType settings = masterSettings.get(relId);
				
				if (settings == null)
					throw new POIXMLException("Master relationship for " + relId + " not found");
				
				if (!(part instanceof XDGFMasterContents))
					throw new POIXMLException("Unexpected masters relationship for " + relId + ": " + part);
				
				XDGFMasterContents contents = (XDGFMasterContents)part;
				contents.onDocumentRead();
				
				XDGFMaster master = new XDGFMaster(settings, contents, _document);
				_masters.put(master.getID(), master);
			}
		} catch (POIXMLException e) {
			throw XDGFException.wrap(this, e);
		}
	}
	
	public Collection<XDGFMaster> getMastersList() {
		return Collections.unmodifiableCollection(_masters.values());
	}
	
	public XDGFMaster getMasterById(long masterId) {
		return _masters.get(masterId);
	}
}
