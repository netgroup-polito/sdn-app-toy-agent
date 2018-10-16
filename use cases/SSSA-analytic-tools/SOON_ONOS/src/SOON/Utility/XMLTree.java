package Utility;


import javax.swing.tree.*;
import org.w3c.dom.*;

public class XMLTree
{
	public XMLTree()
	{
	}
	
	public DefaultMutableTreeNode getJTree(Document doc)
	{
		Element root = (Element)doc.getDocumentElement();
		DefaultMutableTreeNode nodeRoot = new DefaultMutableTreeNode("DSE DB");
		setNode(nodeRoot, root);
		return nodeRoot;
	}
	
	private void setNode(DefaultMutableTreeNode treeNode, Element el)
	{
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("DSE DB");
		
    NodeList elList = el.getChildNodes();
    
    for(int j=0; j<elList.getLength(); j++)
		{
			if(elList.item(j).getNodeType() == Node.ELEMENT_NODE)
			{
				Element ee = (Element)elList.item(j);
				String nodeName = ee.getTagName() + " - " + ee.getAttribute("address");
				DefaultMutableTreeNode nn = new DefaultMutableTreeNode(nodeName);
				treeNode.add(nn);
				setNode(nn, ee);				
			}
		}			
	}
}