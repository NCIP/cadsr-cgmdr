using System;
using System.Windows.Forms;
using System.Xml;
using System.Xml.Linq;

namespace EnterpriseArchitectAddIn
{
    public partial class EAQueryServiceControl : QueryServiceControl.QueryServiceControl
    {
        public EA.Repository m_Repository { set; get; }
        public bool m_IncludeElements { set; get; }
        private XmlNode selectedNode = null;

        public EAQueryServiceControl()
        {
            InitializeComponent();
        }

        protected override void use(object sender, EventArgs e)
        {
            XmlNode srcNode = null;
            string selectedId = null;
            string selectedName = null;
            try
            {
                if (sender is Button)
                {
                    if (((Button)sender).Name == "btnCLSUse")
                    {
                        srcNode = lastClassificationQueryResult;
                        selectedId = getSelectedItem(lstClassificationQueryResult).ID;
                        selectedName = getSelectedItem(lstClassificationQueryResult).NAME;
                    }
                    else if (((Button)sender).Name == "btnUse")
                    {
                        srcNode = lastResult;
                        selectedId = getSelectedItem(lstResults).ID;
                        selectedName = getSelectedItem(lstResults).NAME;
                    }
                    else
                    {
                        return;
                    }
                }
            }
            catch (Exception)
            {
                MessageBox.Show("No element selected");
                return;
            }

            selectedNode = srcNode.SelectSingleNode("/rs:result-set/rs:data-element[rs:names/rs:id=\"" + selectedId + "\"]", nsmanager);
            if (selectedNode == null)
            {
                selectedNode = srcNode.SelectSingleNode("/rs:result-set/rs:concept[rs:names/rs:id=\"" + selectedId + "\"]", nsmanager);
                if (selectedNode != null)
                {
                    //MessageBox.Show("Not a data element");
                    EAUtil.insertConceptRef(m_Repository, XElement.Parse(selectedNode.OuterXml));
                }
                else
                {
                    MessageBox.Show("Result is not a data element");
                }
                return;
            }
            EA.Package package = m_Repository.GetPackageByID(m_Repository.GetCurrentDiagram().PackageID);
            if (package.StereotypeEx == "XSDschema")
            {
                EASubmissionCompleteControl scControl = new EASubmissionCompleteControl();
                scControl.ID = selectedId;
                scControl.PREFERRED = selectedName;
                scControl.OnInsertTopXSDElement += new EventHandler(scControl_OnInsertTopXSDElement);
                scControl.OnInsertTopXSDAttribute += new EventHandler(scControl_OnInsertTopXSDAttribute);
                scControl.Show();
            }
            else
            {
                EAUtil.insertCDE(m_Repository, XElement.Parse(selectedNode.OuterXml), EAUtil.INSERT_XSD_TYPE.NOT_APPLICABLE);
            }
        }

        protected override void useCLS(object sender, EventArgs e)
        {
            use(sender, e);
        }

        private EA.Element getSelectedElement()
        {
            try
            {
                object item;
                EA.ObjectType t = m_Repository.GetTreeSelectedItem(out item);
                EA.Diagram r = (EA.Diagram)item;
                if (r.SelectedObjects.Count != 1)
                {
                    MessageBox.Show("Please select single element");
                    return null;
                }
                EA.DiagramObject dObj = (EA.DiagramObject)r.SelectedObjects.GetAt(0);
                EA.Element el = m_Repository.GetElementByID(dObj.ElementID);
                return el;
            }
            catch (Exception)
            {
                MessageBox.Show("No valid selection");
                return null;
            }
        }

        void scControl_OnInsertTopXSDAttribute(object sender, EventArgs e)
        {
            if (selectedNode == null)
            {
                MessageBox.Show("Error inserting data element");
                return;
            }
            EAUtil.insertCDE(m_Repository, XElement.Parse(selectedNode.OuterXml), EAUtil.INSERT_XSD_TYPE.TOP_LEVEL_ATTRIBUTE);
        }

        void scControl_OnInsertTopXSDElement(object sender, EventArgs e)
        {
            if (selectedNode == null)
            {
                MessageBox.Show("Error inserting data element");
                return;
            }
            EAUtil.insertCDE(m_Repository, XElement.Parse(selectedNode.OuterXml), EAUtil.INSERT_XSD_TYPE.TOP_LEVEL_ELEMENT);
        }
    }
}
