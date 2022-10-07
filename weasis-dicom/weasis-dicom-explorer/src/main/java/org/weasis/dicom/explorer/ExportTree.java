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

import it.cnr.imaa.essi.lablib.gui.checkboxtree.CheckboxTree;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingModel;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Map;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import org.weasis.dicom.explorer.CheckTreeModel.ToolTipTreeNode;
import org.weasis.dicom.codec.KOSpecialElement;
import org.weasis.dicom.codec.DicomMediaIO;
import org.dcm4che3.data.Attributes;
import java.util.Collection;
import org.weasis.dicom.codec.macro.HierachicalSOPInstanceReference;
import org.weasis.dicom.codec.macro.KODocumentModule;
import org.weasis.dicom.codec.macro.SOPInstanceReferenceAndMAC;
import org.weasis.dicom.codec.AbstractKOSpecialElement;
import org.weasis.dicom.codec.DicomSpecialElement;
import org.dcm4che3.data.Tag;
import org.weasis.dicom.codec.DicomImageElement;
import org.weasis.dicom.codec.AbstractKOSpecialElement.Reference;
import org.weasis.dicom.codec.TagD;
import org.weasis.core.api.media.data.TagW;

public class ExportTree extends JScrollPane {

  private final CheckboxTree checkboxTree;
  private final CheckTreeModel checkTreeModel;

  public ExportTree(DicomModel dicomModel) {
    this(new CheckTreeModel(dicomModel));
  }

  public ExportTree(final CheckTreeModel checkTreeModel) {
    this.checkTreeModel = Objects.requireNonNull(checkTreeModel);

    checkboxTree =
        new CheckboxTree(checkTreeModel.getModel()) {
          @Override
          public String getToolTipText(MouseEvent evt) {
            if (getRowForLocation(evt.getX(), evt.getY()) == -1) {
              return null;
            }
            TreePath curPath = getPathForLocation(evt.getX(), evt.getY());
            if (curPath != null) {
              Object object = curPath.getLastPathComponent();
              if (object instanceof ToolTipTreeNode) {
                return ((ToolTipTreeNode) object).getToolTipText();
              }
            }
            return null;
          }
        };

    // Register tooltips
    checkboxTree.setToolTipText("");

    /**
     * At this point checking Paths are supposed to be binded at Series Level but depending on the
     * CheckingMode it may also contain parents treeNode paths.<br>
     * For medical use recommendation is to default select the whole series related to th studies to
     * be analyzed
     */
    TreeCheckingModel checkingModel = checkTreeModel.getCheckingModel();
    TreePath[] checkingPaths = checkTreeModel.getCheckingPaths();
    checkboxTree.setCheckingModel(
        checkingModel); // be aware that checkingPaths is cleared at this point

    // checkingModel.setCheckingMode(checkTreeModel.getCheckingModel().getCheckingMode());
    // -- checkingMode is alreadySet inDicomExport

    if (checkingPaths != null && checkingPaths.length > 0) {
      Set<TreePath> studyPathsSet = new HashSet<>();

      DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) checkTreeModel.getRootNode();
      Enumeration<?> enumTreeNode = ((DefaultMutableTreeNode) rootNode).breadthFirstEnumeration();
      boolean dicomSpecialElem = false;
      while (enumTreeNode.hasMoreElements()) { // iterating nodes under root node
        Object child = enumTreeNode.nextElement();
        if (child instanceof DefaultMutableTreeNode) {
          DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) child;
          if (treeNode.getLevel() != 3) { // 3 stands for Series Level
            continue;
          }

          DefaultMutableTreeNode mediaElem = (DefaultMutableTreeNode) ((Enumeration) treeNode.children()).nextElement();
          Object mediaElemObject = mediaElem.getUserObject();
          if (mediaElemObject instanceof KOSpecialElement) { // if open key object
            String referenceObjectStr = "";
            Set<String> referenceObjectSet  = ((KOSpecialElement) mediaElemObject).getReferencedSOPInstanceUIDSet();
            Iterator referenceObjects = referenceObjectSet.iterator();
            if(referenceObjects.hasNext()) {
              referenceObjectStr = (String) referenceObjects.next();
            }
            TreePath referenceTreePath = findTreePath(referenceObjectStr, rootNode);
            if(referenceTreePath != null) {
              studyPathsSet.add(referenceTreePath);
            }
            
            TreePath checkingPath = new TreePath(mediaElem.getPath());
            studyPathsSet.add(checkingPath);
            dicomSpecialElem = true;
          }
        }
      }

      if (!dicomSpecialElem) {
        for (TreePath checkingPath : checkingPaths) {
          if (checkingPath.getPathCount() == 4) { // 4 stands for Series Level
            studyPathsSet.add(checkingPath.getParentPath());
          }
        }
      }

      if (!studyPathsSet.isEmpty()) {
        TreePath[] studyCheckingPaths = studyPathsSet.toArray(new TreePath[0]);
        checkboxTree.setCheckingPaths(studyCheckingPaths);
      }

      List<TreePath> selectedPaths = checkTreeModel.getDefaultSelectedPaths();
      if (!selectedPaths.isEmpty()) {
        checkboxTree.setSelectionPaths(selectedPaths.toArray(new TreePath[0]));
      }
    }

    expandTree(checkboxTree, checkTreeModel.getRootNode(), 2); // 2 stands for Study Level
    setViewportView(checkboxTree);
  }

  public CheckboxTree getTree() {
    return checkboxTree;
  }

  public CheckTreeModel getModel() {
    return checkTreeModel;
  }

  public static void expandTree(JTree tree, DefaultMutableTreeNode start, int maxDeep) {
    if (maxDeep > 1) {
      Enumeration<?> children = start.children();
      while (children.hasMoreElements()) {
        Object child = children.nextElement();
        if (child instanceof DefaultMutableTreeNode) {
          DefaultMutableTreeNode dtm = (DefaultMutableTreeNode) child;
          if (!dtm.isLeaf()) {
            TreePath tp = new TreePath(dtm.getPath());
            tree.expandPath(tp);
            expandTree(tree, dtm, maxDeep - 1);
          }
        }
      }
    }
  }

  public TreePath findTreePath(String refObjStr, DefaultMutableTreeNode enumRootNode) {
    TreePath referenceCheckingPath = null;
    Enumeration<?> referenceTreeNode = ((DefaultMutableTreeNode) enumRootNode).breadthFirstEnumeration();
    while (referenceTreeNode.hasMoreElements()) {
      Object childRef = referenceTreeNode.nextElement();
      if (childRef instanceof DefaultMutableTreeNode) {
        DefaultMutableTreeNode childTreeNode = (DefaultMutableTreeNode) childRef;
        if (childTreeNode.getLevel() != 3) { // 3 stands for Series Level
          continue;
        }

        Enumeration refTreeEnum = childTreeNode.children();
        while (refTreeEnum.hasMoreElements()) {
          DefaultMutableTreeNode refMediaElem = (DefaultMutableTreeNode) refTreeEnum.nextElement();
          Object refMediaObj = refMediaElem.getUserObject();
          if (refMediaObj instanceof KOSpecialElement) {
            continue;
          }
          Attributes refDicomImgObject = ((DicomImageElement) refMediaObj).getMediaReader().getDicomObject();
          String referenceSOPInstanceUID = "";
          referenceSOPInstanceUID = refDicomImgObject.getString(Tag.SOPInstanceUID);

          if (refObjStr.equals(referenceSOPInstanceUID)) {
            referenceCheckingPath = new TreePath(refMediaElem.getPath());
            break;
          } 
        }
      }
    }
    return referenceCheckingPath;
  }
}
