/*
 * Copyright (c) 2009-2020 Weasis Team and other contributors.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0, or the Apache
 * License, Version 2.0 which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package org.weasis.dicom.explorer;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.dcm4che3.data.Tag;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.explorer.ObservableEvent;
import org.weasis.core.api.explorer.model.DataExplorerModel;
import org.weasis.core.api.gui.util.GuiExecutor;
import org.weasis.core.api.media.MimeInspector;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.api.media.data.MediaElement;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.api.media.data.MediaSeriesGroup;
import org.weasis.core.api.media.data.MediaSeriesGroupNode;
import org.weasis.core.api.media.data.Series;
import org.weasis.core.api.media.data.SeriesThumbnail;
import org.weasis.core.api.media.data.TagW;
import org.weasis.core.api.media.data.Thumbnail;
import org.weasis.core.ui.docking.UIManager;
import org.weasis.core.ui.editor.SeriesViewerFactory;
import org.weasis.core.ui.editor.ViewerPluginBuilder;
import org.weasis.core.ui.editor.image.DefaultView2d;
import org.weasis.core.ui.editor.image.ImageViewerPlugin;
import org.weasis.core.ui.editor.image.SynchData;
import org.weasis.core.ui.model.GraphicModel;
import org.weasis.core.ui.model.graphic.Graphic;
import org.weasis.core.ui.serialize.XmlSerializer;
import org.weasis.core.util.FileUtil;
import org.weasis.dicom.codec.DicomCodec;
import org.weasis.dicom.codec.DicomMediaIO;
import org.weasis.dicom.codec.DicomSpecialElement;
import org.weasis.dicom.codec.TagD;
import org.weasis.dicom.codec.TagD.Level;

public class LoadLocalDicom extends ExplorerTask<Boolean, String> {

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(LoadLocalDicom.class);
  private final File[] files;
  private final DicomModel dicomModel;
  private final boolean recursive;
  private boolean openPlugin;
  private int lastOpenedSeriesNo = -1;
  
  public String thisStudySeriesForXray = null;

  public LoadLocalDicom(File[] files, boolean recursive, DataExplorerModel explorerModel) {
    super(Messages.getString("DicomExplorer.loading"), false);
    if (files == null || !(explorerModel instanceof DicomModel)) {
      throw new IllegalArgumentException("invalid parameters");
    }
    this.dicomModel = (DicomModel) explorerModel;
    this.files = files;
    this.recursive = recursive;
    this.openPlugin = true;
  }

  @Override
  protected Boolean doInBackground() throws Exception {
    dicomModel.firePropertyChange(
        new ObservableEvent(ObservableEvent.BasicAction.LOADING_START, dicomModel, null, this));
    addSelectionAndnotify(files, true);
    return true;
  }

  @Override
  protected void done() {
    dicomModel.firePropertyChange(
        new ObservableEvent(ObservableEvent.BasicAction.LOADING_STOP, dicomModel, null, this));
    LOGGER.info("End of loading DICOM locally");
  }

  public void addSelectionAndnotify(File[] file, boolean firstLevel) {
    if (file == null || file.length < 1) {
      return;
    }
    final ArrayList<SeriesThumbnail> thumbs = new ArrayList<>();
    final ArrayList<File> folders = new ArrayList<>();

    DefaultView2d.totalNoOfImages = file.length;
    DefaultView2d.studyLoaded = false;
    for (File value : file) {
      if (isCancelled()) {
        return;
      }

      if (value == null) {
        continue;
      } else if (value.isDirectory()) {
        if (firstLevel || recursive) {
          folders.add(value);
        }
      } else {
        if (value.canRead()) {
          if (FileUtil.isFileExtensionMatching(value, DicomCodec.FILE_EXTENSIONS)
              || MimeInspector.isMatchingMimeTypeFromMagicNumber(
                  value, DicomMediaIO.DICOM_MIMETYPE)) {
            DicomMediaIO loader = new DicomMediaIO(value);
            if (loader.isReadableDicom()) {
            	File gpxFile = new File(value.getPath() + ".xml");
                GraphicModel graphicModel = XmlSerializer.readPresentationModel(gpxFile);
                if (graphicModel != null) {
                  loader.setTag(TagW.PresentationModel, graphicModel);
                  for(Graphic gModel: graphicModel.getModels()) {
        			  gModel.getLayer().setSerializable(true);
        		  	}
                }
                
            	// Issue: must handle adding image to viewer and building thumbnail (middle image)
              SeriesThumbnail t = buildDicomStructure(loader, openPlugin);
              if (t != null) {
                thumbs.add(t);
              }

              
            }
          }
        }
      }
    }
    lastOpenedSeriesNo = -1;
    
    if ( UIManager.VIEWER_PLUGINS.size() > 0 && ((ImageViewerPlugin<ImageElement>) UIManager.VIEWER_PLUGINS.get(0)) != null && 
    		SynchData.Mode.STACK.equals(((ImageViewerPlugin<ImageElement>) UIManager.VIEWER_PLUGINS.get(0)).getSynchView().getSynchData().getMode()) &&
    		((ImageViewerPlugin<ImageElement>) UIManager.VIEWER_PLUGINS.get(0)).getImagePaneGridSize() > 1 && thumbs.size() > 0 ) {
//    	System.out.println((ThumbnailMouseAndKeyAdapter)thumbs.get(1).getMouseListeners()[0]);
//    ((ThumbnailMouseAndKeyAdapter)thumbs.get(1).getMouseListeners()[0]).mouseClicked(null);
    	
    	
    	// working
//    	System.out.println(thumbs.get(1).getMouseListeners().length);
//    	MouseListener[] l = thumbs.get(0).getMouseListeners();
//    ThumbnailMouseAndKeyAdapter tt = (ThumbnailMouseAndKeyAdapter)(l[3]);
//    System.out.println(tt);
//    tt.mouseClicked(new MouseEvent(getBar(), 0, 0, 0, 0, 0, 1, false));
    	
    	// sort the thumbnails
    	ArrayList<SeriesThumbnail> sortedThumbs = new ArrayList<SeriesThumbnail>(thumbs.size());
    	for(int i=0; i<thumbs.size(); i++) {
    		if(i == 0) {
    			sortedThumbs.add(0, thumbs.get(0));
    			continue;
    		}
    		
    		int thisInstanceNo = TagD.getTagValue(thumbs.get(i).getSeries(), Tag.SeriesNumber, Integer.class);
    		boolean isThumbInserted = false;
    		for(int j=0; j<sortedThumbs.size(); j++) {
    			if(thisInstanceNo > TagD.getTagValue(sortedThumbs.get(j).getSeries(), Tag.SeriesNumber, Integer.class)) continue;
    			else {
    				sortedThumbs.add(j, thumbs.get(i));
    				isThumbInserted = true;
    				break;
    			}
    		}
    		if(!isThumbInserted) sortedThumbs.add(thumbs.get(i));
    	}
    	
    	int gridSize = ((ImageViewerPlugin<ImageElement>) UIManager.VIEWER_PLUGINS.get(0)).getImagePaneGridSize();
    	for(int i=1; i<gridSize; i++) {
    		((ThumbnailMouseAndKeyAdapter)sortedThumbs.get(i).getMouseListeners()[3]).mouseClicked(new MouseEvent(getBar(), 0, 0, 0, 0, 0, 1, false));
    	}
//	    ((ThumbnailMouseAndKeyAdapter)sortedThumbs.get(3).getMouseListeners()[3]).mouseClicked(new MouseEvent(getBar(), 0, 0, 0, 0, 0, 1, false));
    
    }
    
    if(DefaultView2d.studyModality != null && DefaultView2d.studyModality.equals("CR")) {
		if(thumbs.size() > 0) {
			List<MediaElement> list = thumbs.get(0).getSeries().getSortedMedias(null);
			Collections.sort(list, (l1, l2) -> TagD.getTagValue(l1, Tag.SeriesNumber, Integer.class) - TagD.getTagValue(l2, Tag.SeriesNumber, Integer.class));
		}
    }
    
    for (final SeriesThumbnail t : thumbs) {
      MediaSeries<MediaElement> series = t.getSeries();
      
      // Avoid rebuilding most of CR series thumbnail
      if (series != null && series.size(null) > 2) {
        GuiExecutor.instance().execute(t::reBuildThumbnail);
      }
    }
    for (File folder : folders) {
      addSelectionAndnotify(folder.listFiles(), false);
    }
  }

  private SeriesThumbnail buildDicomStructure(DicomMediaIO dicomReader, boolean open) {
    SeriesThumbnail thumb = null;
    String studyUID = (String) dicomReader.getTagValue(TagD.getUID(Level.STUDY));
    String patientPseudoUID = (String) dicomReader.getTagValue(TagD.getUID(Level.PATIENT));
    MediaSeriesGroup patient =
        dicomModel.getHierarchyNode(MediaSeriesGroupNode.rootNode, patientPseudoUID);
    if (patient == null) {
      MediaSeriesGroup study = dicomModel.getStudyNode(studyUID);
      if (study == null) {
        patient =
            new MediaSeriesGroupNode(
                TagW.PatientPseudoUID, patientPseudoUID, DicomModel.patient.getTagView());
        dicomReader.writeMetaData(patient);
        dicomModel.addHierarchyNode(MediaSeriesGroupNode.rootNode, patient);
        LOGGER.info("Adding patient: {}", patient);
      } else {
        patient = dicomModel.getParent(study, DicomModel.patient);
        LOGGER.warn(
            "DICOM patient attributes are inconsistent! Name or ID is different within an exam.");
      }
    }

    MediaSeriesGroup study = dicomModel.getHierarchyNode(patient, studyUID);
    if (study == null) {
      study =
          new MediaSeriesGroupNode(
              TagD.getUID(Level.STUDY), studyUID, DicomModel.study.getTagView());
      dicomReader.writeMetaData(study);
      dicomModel.addHierarchyNode(patient, study);
    }

    String seriesUID = (String) dicomReader.getTagValue(TagD.get(Tag.SeriesInstanceUID));
    Integer xraySeriesNo = null;
    Series dicomSeries;
    
    if(DefaultView2d.studyModality == null) { // first file in the dicom to scan for modality
    	DefaultView2d.studyModality = dicomReader.getTagValue(TagD.get(Tag.Modality)).toString().equals("DX") ? "CR" : dicomReader.getTagValue(TagD.get(Tag.Modality)).toString();
    }
    
    if(DefaultView2d.studyModality != null && DefaultView2d.studyModality.equals("CR")) {
    	if(thisStudySeriesForXray == null) {
			Series dicomSeriesOfFirstFile = dicomReader.buildSeries(seriesUID);
			dicomReader.writeMetaData(dicomSeriesOfFirstFile);
			Integer thisSeriesNo = TagD.getTagValue(dicomSeriesOfFirstFile, Tag.SeriesNumber, Integer.class);
			Integer thisInstanceNo = TagD.getTagValue(dicomReader, Tag.InstanceNumber, Integer.class);
			xraySeriesNo = (thisSeriesNo * 1000) + thisInstanceNo;
			
			thisStudySeriesForXray = seriesUID;
			DicomModel.xraySeriesStore = thisStudySeriesForXray;
    	} else {
    		if(seriesUID.equals(thisStudySeriesForXray)) {
    			Series dicomSeriesOfFirstFile = dicomReader.buildSeries(seriesUID);
    			dicomReader.writeMetaData(dicomSeriesOfFirstFile);
    			Integer thisSeriesNo = TagD.getTagValue(dicomSeriesOfFirstFile, Tag.SeriesNumber, Integer.class);
    			Integer thisInstanceNo = TagD.getTagValue(dicomReader, Tag.InstanceNumber, Integer.class);
    			xraySeriesNo = (thisSeriesNo * 1000) + thisInstanceNo;    			
    		}
    	}
	    dicomSeries = (Series) dicomModel.getHierarchyNode(study, thisStudySeriesForXray);
    } else
    	dicomSeries = (Series) dicomModel.getHierarchyNode(study, seriesUID);
    
    try {
      if (dicomSeries == null) {
        dicomSeries = dicomReader.buildSeries(seriesUID);
        dicomSeries.setTag(TagW.ExplorerModel, dicomModel);
        dicomReader.writeMetaData(dicomSeries);
        dicomModel.addHierarchyNode(study, dicomSeries);
        MediaElement[] medias = dicomReader.getMediaElement();
        if (medias != null) {
          for (MediaElement media : medias) {
            dicomModel.applySplittingRules(dicomSeries, media);
            if(DefaultView2d.studyModality != null && DefaultView2d.studyModality.equals("CR")) media.setTag(TagD.get(Tag.SeriesNumber), xraySeriesNo);
          }
          if (medias.length > 0) {
            dicomSeries.setFileSize(dicomSeries.getFileSize() + medias[0].getLength());
          }
        }

        // Load image and create thumbnail in this Thread
        SeriesThumbnail t = (SeriesThumbnail) dicomSeries.getTagValue(TagW.Thumbnail);
        if (t == null) {
          t = DicomExplorer.createThumbnail(dicomSeries, dicomModel, Thumbnail.DEFAULT_SIZE);
          dicomSeries.setTag(TagW.Thumbnail, t);
          Optional.ofNullable(t).ifPresent(SeriesThumbnail::repaint);
        }

        if (DicomModel.isSpecialModality(dicomSeries)) {
          dicomModel.addSpecialModality(dicomSeries);
          Arrays.stream(medias)
              .filter(DicomSpecialElement.class::isInstance)
              .map(DicomSpecialElement.class::cast)
              .findFirst()
              .ifPresent(
                  d ->
                      dicomModel.firePropertyChange(
                          new ObservableEvent(
                              ObservableEvent.BasicAction.UPDATE, dicomModel, null, d)));
        } else {
          dicomModel.firePropertyChange(
              new ObservableEvent(ObservableEvent.BasicAction.ADD, dicomModel, null, dicomSeries));
        }

        // After the thumbnail is sent to interface, it will be return to be rebuilt later
        thumb = t;
        int thisSeriesNo = (Integer)(t.getSeries().getTagValue(TagD.get(Tag.SeriesNumber)));
        
        if(lastOpenedSeriesNo != -1 &&  thisSeriesNo < lastOpenedSeriesNo) {
        	open = true;
        }

        Integer splitNb = (Integer) dicomSeries.getTagValue(TagW.SplitSeriesNumber);
        if (splitNb != null) {
          dicomModel.firePropertyChange(
              new ObservableEvent(
                  ObservableEvent.BasicAction.UPDATE, dicomModel, null, dicomSeries));
        }

        if (open) {
          SeriesViewerFactory plugin = UIManager.getViewerFactory(dicomSeries.getMimeType());
          if (plugin != null && !(plugin instanceof MimeSystemAppFactory)) {
            openPlugin = false;
            lastOpenedSeriesNo = thisSeriesNo;
            ViewerPluginBuilder.openSequenceInPlugin(plugin, dicomSeries, dicomModel, true, true);
          } else if (plugin != null) {
            // Send event to select the related patient in Dicom Explorer.
            dicomModel.firePropertyChange(
                new ObservableEvent(
                    ObservableEvent.BasicAction.SELECT, dicomModel, null, dicomSeries));
          }
        }
      } else {
        // Test if SOPInstanceUID already exists
        if (isSOPInstanceUIDExist(
            study,
            dicomSeries,
            seriesUID,
            TagD.getTagValue(dicomReader, Tag.SOPInstanceUID, String.class))) {
          return null;
        }
        MediaElement[] medias = dicomReader.getMediaElement();
        if (medias != null) {
          for (MediaElement media : medias) {
            dicomModel.applySplittingRules(dicomSeries, media);
            if(xraySeriesNo != null && DefaultView2d.studyModality != null && DefaultView2d.studyModality.equals("CR")) media.setTag(TagD.get(Tag.SeriesNumber), xraySeriesNo);
          }
          if (medias.length > 0) {
            dicomSeries.setFileSize(dicomSeries.getFileSize() + medias[0].getLength());
            // Refresh the number of images on the thumbnail
            Thumbnail t = (Thumbnail) dicomSeries.getTagValue(TagW.Thumbnail);
            if (t != null) {
              t.repaint();
            }
          }

          if (DicomModel.isSpecialModality(dicomSeries)) {
            dicomModel.addSpecialModality(dicomSeries);
            Arrays.stream(medias)
                .filter(DicomSpecialElement.class::isInstance)
                .map(DicomSpecialElement.class::cast)
                .findFirst()
                .ifPresent(
                    d ->
                        dicomModel.firePropertyChange(
                            new ObservableEvent(
                                ObservableEvent.BasicAction.UPDATE, dicomModel, null, d)));
          }

          // If Split series or special DICOM element update the explorer view and View2DContainer
          Integer splitNb = (Integer) dicomSeries.getTagValue(TagW.SplitSeriesNumber);
          if (splitNb != null) {
            dicomModel.firePropertyChange(
                new ObservableEvent(
                    ObservableEvent.BasicAction.UPDATE, dicomModel, null, dicomSeries));
          }
          if ( UIManager.VIEWER_PLUGINS.size() > 0 && ((ImageViewerPlugin<ImageElement>) UIManager.VIEWER_PLUGINS.get(0)) != null && 
        		  SynchData.Mode.TILE.equals(((ImageViewerPlugin<ImageElement>) UIManager.VIEWER_PLUGINS.get(0)).getSynchView().getSynchData().getMode()) ) {
	          SeriesViewerFactory plugin = UIManager.getViewerFactory(dicomSeries.getMimeType());
	          ViewerPluginBuilder.openSequenceInPlugin(plugin, dicomSeries, dicomModel, true, true);
          }
        }
      }
    } catch (Exception e) {
    	e.printStackTrace();
      LOGGER.error("Build DicomModel", e);
    } finally {
      // dicomReader.reset();
    }
    return thumb;
  }

  private boolean isSOPInstanceUIDExist(
      MediaSeriesGroup study, Series dicomSeries, String seriesUID, Object sopUID) {
    TagW sopTag = TagD.getUID(Level.INSTANCE);
    if (dicomSeries.hasMediaContains(sopTag, sopUID)) {
      return true;
    }
    Object splitNb = dicomSeries.getTagValue(TagW.SplitSeriesNumber);
    if (splitNb != null && study != null) {
      String uid = TagD.getTagValue(dicomSeries, Tag.SeriesInstanceUID, String.class);
      if (uid != null) {
        Collection<MediaSeriesGroup> seriesList = dicomModel.getChildren(study);
        for (MediaSeriesGroup group : seriesList) {
          if (dicomSeries != group && group instanceof Series) {
            Series s = (Series) group;
            if (uid.equals(TagD.getTagValue(group, Tag.SeriesInstanceUID))) {
              if (s.hasMediaContains(sopTag, sopUID)) {
                return true;
              }
            }
          }
        }
      }
    }
    return false;
  }
}
