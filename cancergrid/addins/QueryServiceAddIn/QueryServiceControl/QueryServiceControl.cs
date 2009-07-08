using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Drawing;
using System.Linq;
using System.Windows.Forms;
using System.Xml;
using System.Xml.Linq;

namespace QueryServiceControl
{
    public partial class QueryServiceControl : UserControl
    {
        private BackgroundWorker bgWorker;
        private BackgroundWorker searchWorker;
        private BackgroundWorker searchCLSWorker;

        private QueryServiceManager.QueryServiceManager qsm = null;
        private List<QueryServiceManager.query_service> resources = null;
        private List<QueryServiceManager.classification_scheme> classification_schemes = null;

        protected XmlDocument lastResult = null;
        protected XmlDocument lastClassificationQueryResult = null;
        protected XmlNamespaceManager nsmanager = null;
        protected int currentPage = 0;
        protected int pageSize = 20;

        protected int currentPageCLS = 0;
        protected int pageSizeCLS = 20;

        public QueryServiceControl()
        {
            InitializeComponent();
            qsm = new QueryServiceManager.QueryServiceManager();

            bgWorker = new BackgroundWorker();
            bgWorker.DoWork += new DoWorkEventHandler(bgWorker_DoWork);
            bgWorker.RunWorkerCompleted += new RunWorkerCompletedEventHandler(bgWorker_RunWorkerCompleted);

            searchWorker = new BackgroundWorker();
            searchWorker.WorkerSupportsCancellation = true;
            searchWorker.DoWork += new DoWorkEventHandler(searchWorker_DoWork);
            searchWorker.RunWorkerCompleted += new RunWorkerCompletedEventHandler(searchWorker_RunWorkerCompleted);

            searchCLSWorker = new BackgroundWorker();
            searchCLSWorker.WorkerSupportsCancellation = true;
            searchCLSWorker.DoWork += new DoWorkEventHandler(searchCLSWorker_DoWork);
            searchCLSWorker.RunWorkerCompleted += new RunWorkerCompletedEventHandler(searchCLSWorker_RunWorkerCompleted);
        }

        void searchCLSWorker_RunWorkerCompleted(object sender, RunWorkerCompletedEventArgs e)
        {
            if (e.Error == null)
            {
                
            }
        }

        void searchCLSWorker_DoWork(object sender, DoWorkEventArgs e)
        {
            searchCLS(sender, e);
        }

        void searchWorker_RunWorkerCompleted(object sender, RunWorkerCompletedEventArgs e)
        {
            if (e.Error == null)
            {

            }
        }

        void searchWorker_DoWork(object sender, DoWorkEventArgs e)
        {
            search(sender, e);
        }

        void bgWorker_RunWorkerCompleted(object sender, RunWorkerCompletedEventArgs e)
        {
            if (e.Error == null)
            {
                if (resources != null)
                {
                    cbResources.DataSource = resources;
                    cbResources.DisplayMember = "name";
                    cbResources.ValueMember = "name";
                    cbResources.SelectedIndex = 0;
                }

                if (classification_schemes != null)
                {
                    cbClassificationSchemes.DataSource = classification_schemes;
                    cbClassificationSchemes.DisplayMember = "Value";
                    cbClassificationSchemes.ValueMember = "uri";
                    cbClassificationSchemes.SelectedIndex = 0;
                    updateClassification_Tree();
                }

                btnSearch.Enabled = true;
                btnSearchCLS.Enabled = true;

                //statusMsg.Text = "";
                SetStatus("");
            }
            else
            {
                MessageBox.Show("Error loading Query Service panel: "+e.Error.Message);
            }
        }

        void bgWorker_DoWork(object sender, DoWorkEventArgs e)
        {
            InitResources();
            InitClassificationSchemes();
        }

        private void QueryServiceControl_Load(object sender, EventArgs e)
        {
            //statusMsg.ForeColor = Color.Blue;
            //statusMsg.Text = "Initializing...";
            SetStatus("Initializing...");

            btnSearch.Enabled = false;
            btnSearchCLS.Enabled = false;
            bgWorker.RunWorkerAsync();
        }

        private void InitResources()
        {
            try
            {
                resources = qsm.listResourcesAsXml().@return.resources.ToList<QueryServiceManager.query_service>();
                resources.RemoveAll(NotDataElementAndNotConcept);
            }
            catch (Exception)
            {
                MessageBox.Show("Fail to initialize query resources.");
            }
        }

        private static bool NotDataElementAndNotConcept(QueryServiceManager.query_service qs)
        {
            return (qs.category != QueryServiceManager.category.CDE && qs.category != QueryServiceManager.category.CONCEPT);
        }

        private void InitClassificationSchemes()
        {
            try
            {
                QueryServiceManager.query query = new QueryServiceManager.query();
                query.resource = "cgMDR-Classification-Schemes";
                QueryServiceManager.@return r = qsm.query(query);
                if (r.resultset.Items.Length > 0)
                {
                    classification_schemes = new List<QueryServiceManager.classification_scheme>();
                    foreach (QueryServiceManager.classification_scheme cs in r.resultset.Items)
                    {
                        classification_schemes.Add(cs);
                    }
                }
            }
            catch (Exception)
            {
                MessageBox.Show("Fail to initialize classification resources");
            }
        }

        private void cbClassificationSchemes_SelectedIndexChanged(object sender, EventArgs e)
        {
            updateClassification_Tree();
        }

        private void updateClassification_Tree()
        {
            try
            {
                QueryServiceManager.query query = new QueryServiceManager.query();
                query.resource = "cgMDR-Classification-Tree";
                query.term = (string)cbClassificationSchemes.SelectedValue;
                QueryServiceManager.@return r = qsm.query(query);
                if (r.resultset.Items.Length != 1)
                {
                    MessageBox.Show("Error getting classification tree for: " + query.term);
                }

                QueryServiceManager.node root = (QueryServiceManager.node)r.resultset.Items[0];
                classificationTree.BeginUpdate();
                TreeNode rootNode = buildTree(root);
                classificationTree.Nodes.Clear();
                classificationTree.Nodes.Add(rootNode);
                classificationTree.EndUpdate();
                rootNode.Expand();
            }
            catch (Exception)
            {
                //MessageBox.Show("cbClassificationSchemes_SelectedIndexChanged: " + ex.Message);
            }
        }

        protected void search(object sender, EventArgs e)
        {
            try
            {
                if (txtTerm.Text == null || txtTerm.Text.Length == 0)
                {
                    //statusMsg.ForeColor = Color.Red;
                    //statusMsg.Text = "No search term.";
                    //statusMsg.Update();
                    SetErrorStatus("No search term.");
                    return;
                }

                btnAnnotate.Enabled = false;
                btnUse.Enabled = false;
                btnBack.Enabled = false;
                btnForward.Enabled = false;
                lstResults.Items.Clear();
                lstResults.Update();
                wbDetailsDef.DocumentText = "";
                wbDetailsPropsValues.DocumentText = "";

                //statusMsg.ForeColor = Color.Blue;
                //statusMsg.Text = "Querying...";
                //statusMsg.Update();
                SetStatus("Querying...");
                this.Cursor = Cursors.WaitCursor;

                QueryServiceManager.query query = new QueryServiceManager.query();
                query.resource = cbResources.SelectedValue.ToString();

                query.term = txtTerm.Text;
                if (currentPage == 0)
                {
                    query.startIndex = currentPage;
                }
                else
                {
                    query.startIndex = currentPage * pageSize;
                }
                //Workaround for SDK caused caDSR API bug.
                //Double page size to have full page after filtering.
                query.numResults = pageSize*2;
                //if (currentPage > 0) 
                //    query.numResults = 200+(pageSize*currentPage)+1;

                string response = qsm.queryString(query);

                lastResult = new XmlDocument();
                lastResult.LoadXml(response);

                nsmanager = new XmlNamespaceManager(lastResult.NameTable);
                nsmanager.AddNamespace("rs", "http://cancergrid.org/schema/result-set");

                int rstSize = 8;
                string[] resultSetTypes = new string[rstSize];
                resultSetTypes[0] = "data-element";
                resultSetTypes[1] = "object-class";
                resultSetTypes[2] = "property-expanded";
                resultSetTypes[3] = "representation-term";
                resultSetTypes[4] = "data-element-concept";
                resultSetTypes[5] = "conceptual-domain";
                resultSetTypes[6] = "values";
                resultSetTypes[7] = "concept";
                
                XmlNodeList nodeList = null;
                for (int i = 0; i < rstSize; i++)
                {
                    nodeList = lastResult.DocumentElement.SelectNodes("/rs:result-set/rs:"+resultSetTypes[i], nsmanager);
                    //if (i > 6)
                    //    btnAnnotate.Enabled = false;
                    //else
                        btnAnnotate.Enabled = true;

                    if (nodeList != null && nodeList.Count > 0)
                        break;

                }
                if (nodeList == null || nodeList.Count == 0)
                {
                    //statusMsg.Text = "No result";
                    SetStatus("No result");
                    this.Cursor = Cursors.Default;
                    return;
                }

                //statusMsg.Text = "Query completed.";
                SetStatus("Listing results");

                listResults(nodeList, lstResults);
                btnUse.Enabled = true;

                if (currentPage > 0)
                {
                    btnBack.Enabled = true;
                }

                if (nodeList.Count > pageSize)
                {
                    btnForward.Enabled = true;
                }
                SetStatus("Query Completed");
            }
            catch (Exception ex)
            {
                MessageBox.Show(ex.ToString());
                //statusMsg.ForeColor = Color.Red;
                //statusMsg.Text = "Query fail";
                SetErrorStatus("Query fail");

            }
            this.Cursor = Cursors.Default;
        }

        private void listResults(XmlNodeList results, ListBox target)
        {
            target.Items.Clear();
            bool filtered = false;

            int itemsShown = 0;

            string[] restrictedW = new string[7];
            restrictedW[0] = "CMTE APPROVED";
            restrictedW[1] = "CMTE SUBMTD";
            restrictedW[2] = "CMTE SUBMTD USED";
            restrictedW[3] = "RETIRED ARCHIVED";
            restrictedW[4] = "RETIRED PHASED OUT";
            restrictedW[5] = "RETIRED WITHDRAWN";
            restrictedW[6] = "RETIRED DELETED";

            foreach (XmlNode node in results)
            {

                bool complex = false;
                bool testContext = false;
                bool retiredWorkflow = false;

                string context = "";
                string workflow = "";
                string registration = "";

                if (node.SelectSingleNode("rs:context", nsmanager) != null)
                {
                    context = node.SelectSingleNode("rs:context", nsmanager).InnerXml;
                    if (context.ToLower().Equals("test") || context.ToLower().Equals("training"))
                        testContext = true;
                }
                if (!testContext)
                {
                    if (node.SelectSingleNode("rs:workflow-status", nsmanager) != null) {
                        workflow = node.SelectSingleNode("rs:workflow-status", nsmanager).InnerXml;
                        string wku = workflow.ToUpper();

                        for (int i = 0; i < restrictedW.Length; i++)
                        {
                            if (wku.Contains(restrictedW[i]))
                            {
                                retiredWorkflow = true;
                                break;
                            }
                        }
                    }

                    if (!retiredWorkflow)
                    {
                        itemsShown = itemsShown + 1;
                        string id = node.SelectSingleNode("rs:names/rs:id", nsmanager).InnerXml;
                        string name = node.SelectSingleNode("rs:names/rs:preferred", nsmanager).InnerXml;
                        string ver = "";
                        if (id.Contains("-CADSR-"))
                        {
                            string[] idarr = id.Split('-');
                            //id = idarr[idarr.Length - 2] + " v." + idarr[idarr.Length - 1];
                            ver = "v." + idarr[idarr.Length - 1];
                        }
                        name += " " + ver;

                        if (node.SelectSingleNode("rs:registration-status", nsmanager) != null)
                        {
                            complex = true;
                            registration = node.SelectSingleNode("rs:registration-status", nsmanager).InnerXml;
                            name += "  (" + registration;
                        }
                        
                        if (workflow.Length > 0)
                        {
                            complex = true;
                            name += ":" + workflow;
                        }

                        if (context.Length > 0)
                        {
                            complex = true;
                            name += ":" + context;
                        }

                        if (complex)
                            name += ")";

                        target.Items.Add(new QueryListItem(id, name));
                    }
                }
                if (itemsShown == pageSize)
                    break;

                if (retiredWorkflow || testContext)
                    filtered = true;
            }

            if (target.Items.Count == pageSize + 1)
            {
                target.Items.RemoveAt(pageSize);
            }
            //if (filtered)
            //    SetStatus("Query Completed (Retired items filtered)");
            //else
            //    SetStatus("Query Completed");

            target.DisplayMember = "NAME";
            target.ValueMember = "ID";          
        }

        protected QueryListItem getSelectedItem(ListBox lb)
        {
            return (QueryListItem)lb.SelectedItem;
        }

        protected virtual void use(object sender, EventArgs e)
        {
            throw new NotImplementedException("This function has not been implmented.");
        }

        protected virtual void useCLS(object sender, EventArgs e)
        {
            throw new NotImplementedException("This function has not been implmented.");
        }

        private void txtTerm_KeyPress(object sender, KeyPressEventArgs e)
        {
            if (e.KeyChar == (char)13)
            {
                search(sender, e);
            }
        }

        private void btnForward_Click(object sender, EventArgs e)
        {
            currentPage++;
            //if (searchWorker.IsBusy)
            //{
            //    searchWorker.CancelAsync();
            //}
            //searchWorker.RunWorkerAsync();
            search(sender, e);
        }

        private void btnSearch_Click(object sender, EventArgs e)
        {
            currentPage = 0;
            //if (searchWorker.IsBusy)
            //{
            //    searchWorker.CancelAsync();
            //}
            //searchWorker.RunWorkerAsync();
            search(sender, e);
        }

        private void btnBack_Click(object sender, EventArgs e)
        {
            currentPage--;
            //if (searchWorker.IsBusy)
            //{
            //    searchWorker.CancelAsync();
            //}
            //searchWorker.RunWorkerAsync();
            search(sender, e);
        }

        private TreeNode buildTree(QueryServiceManager.node root)
        {
            TreeNode newNode = new TreeNode();
            newNode.Name = root.label;
            newNode.Text = root.label;
            newNode.Tag = root.prefix+"#"+root.id;

            if (root.node1 == null || root.node1.Length == 0)
            {
                return newNode;
            }

            foreach (QueryServiceManager.node n in root.node1)
            {
                newNode.Nodes.Add(buildTree(n));
            }

            return newNode;
        }

        private void searchCLS(object sender, EventArgs e)
        {
            try
            {
                if (classificationTree.SelectedNode == null)
                {
                    //statusMsgCLS.ForeColor = Color.Red;
                    //statusMsgCLS.Text = "No node selected";
                    //statusMsgCLS.Update();
                    SetErrorStatus("No node selected");
                    return;
                }
                TreeNode selectedNode = classificationTree.SelectedNode;
                QueryServiceManager.query query = new QueryServiceManager.query();
                query.resource = "cgMDR-with-Classification";
                query.term = "*";
                query.src = selectedNode.Tag.ToString();
                if (currentPageCLS == 0)
                {
                    query.startIndex = currentPageCLS;
                }
                else
                {
                    query.startIndex = currentPageCLS * pageSizeCLS;
                }
                query.numResults = pageSizeCLS + 1;
                
                lstClassificationQueryResult.Items.Clear();
                wbClassificationQueryResultDef.DocumentText = "";
                wbClassificationQueryResultValueDomain.DocumentText = "";

                //statusMsgCLS.ForeColor = Color.Blue;
                //statusMsgCLS.Text = "Querying...";
                //statusMsgCLS.Update();
                SetStatus("Querying...");
                this.Cursor = Cursors.WaitCursor;
                btnCLSUse.Enabled = false;
                string response = qsm.queryString(query);

                lastClassificationQueryResult = new XmlDocument();
                lastClassificationQueryResult.LoadXml(response);

                nsmanager = new XmlNamespaceManager(lastClassificationQueryResult.NameTable);
                nsmanager.AddNamespace("rs", "http://cancergrid.org/schema/result-set");

                XmlNodeList nodeList = lastClassificationQueryResult.DocumentElement.SelectNodes("/rs:result-set/rs:data-element", nsmanager);
                if (nodeList == null || nodeList.Count == 0)
                {
                    //statusMsgCLS.Text = "No result";
                    //statusMsgCLS.Update();
                    SetStatus("No result");
                    this.Cursor = Cursors.Default;
                    return;
                }

                listResults(nodeList, lstClassificationQueryResult);
                if (nodeList.Count > 0)
                {
                    btnCLSUse.Enabled = true;
                }

                if (currentPageCLS > 0)
                {
                    btnBackCLS.Enabled = true;
                }

                if (nodeList.Count >= pageSizeCLS)
                {
                    btnForwardCLS.Enabled = true;
                }

                //statusMsgCLS.Text = "Query complete";
                //statusMsgCLS.Update();
                SetStatus("Query complete");
            }
            catch (Exception ex)
            {
                MessageBox.Show(ex.Message);
                //statusMsg.ForeColor = Color.Red;
                //statusMsg.Text = "Query fail";
                //statusMsgCLS.Update();
                SetErrorStatus("Query fail");
            }
            this.Cursor = Cursors.Default;
        }

        private void classificationTree_NodeMouseDoubleClick(object sender, TreeNodeMouseClickEventArgs e)
        {
            searchCLS(sender, e);
        }

        private void updateDetails(object sender, EventArgs e)
        {
            try
            {
                string definition = null;
                string values = null;
                string props = null;
                string other = null;
                string nodeId = null;
                XmlNode defNode = null;
                XmlNode propsNode = null;
                XmlNode vdNode = null;

                XmlNode ccNode = null;
                XmlNode ocNode = null;
                XmlNode propNode = null;

                
                if (sender.Equals(lstClassificationQueryResult))
                {
                    vdNode = lastClassificationQueryResult.DocumentElement.SelectSingleNode("/rs:result-set/*[rs:names/rs:id = '" + getSelectedItem(lstClassificationQueryResult).ID + "']/rs:values", nsmanager);
                    defNode = lastClassificationQueryResult.DocumentElement.SelectSingleNode("/rs:result-set/*[rs:names/rs:id = '" + getSelectedItem(lstClassificationQueryResult).ID + "']/rs:definition", nsmanager);
                }
                else if (sender.Equals(lstResults))
                {
                    vdNode = lastResult.DocumentElement.SelectSingleNode("/rs:result-set/rs:data-element[rs:names/rs:id = '" + getSelectedItem(lstResults).ID + "']/rs:values", nsmanager);
                    propsNode = lastResult.DocumentElement.SelectSingleNode("/rs:result-set/rs:concept[rs:names/rs:id = '" + getSelectedItem(lstResults).ID + "']/rs:properties", nsmanager);
                    if (propsNode != null)
                    {
                        nodeId = getSelectedItem(lstResults).ID;
                        nodeId = nodeId.Substring(nodeId.LastIndexOf('-') + 1);
                        
                    }
                    defNode = lastResult.DocumentElement.SelectSingleNode("/rs:result-set/*[rs:names/rs:id = '" + getSelectedItem(lstResults).ID + "']/rs:definition", nsmanager);
                    ccNode = lastResult.DocumentElement.SelectSingleNode("/rs:result-set/*[rs:names/rs:id = '" + getSelectedItem(lstResults).ID + "']/rs:conceptCollection", nsmanager);
                    ocNode = lastResult.DocumentElement.SelectSingleNode("/rs:result-set/rs:data-element-concept[rs:names/rs:id = '" + getSelectedItem(lstResults).ID + "']/rs:object-class", nsmanager);
                    propNode = lastResult.DocumentElement.SelectSingleNode("/rs:result-set/rs:data-element-concept[rs:names/rs:id = '" + getSelectedItem(lstResults).ID + "']/rs:property-expanded", nsmanager); 
                    if (ccNode == null || ccNode.InnerXml.Length == 0)
                        ccNode = lastResult.DocumentElement.SelectSingleNode("/rs:result-set/rs:property-expanded[rs:names/rs:id = '" + getSelectedItem(lstResults).ID + "']/rs:conceptCollection", nsmanager);
                    if (vdNode == null || vdNode.InnerXml.Length == 0)
                        vdNode = lastResult.DocumentElement.SelectSingleNode("/rs:result-set/rs:values[rs:names/rs:id = '" + getSelectedItem(lstResults).ID + "']", nsmanager);
                    
                }
                else
                {
                    return;
                }
                if (defNode == null || defNode.InnerXml.Length == 0)
                {
                    definition = "<div style=\"font-size: 12px; text-aligh: justify;\">(No definition supplied)</div>";
                }
                else
                {
                    string def = defNode.InnerXml;
                    if (def.Contains("def-source"))
                    {
                        def = def.Trim().Replace("&gt;", ">").Replace("&lt;", "<").Replace("<![CDATA[", "").Replace("]]>", "");
                        XmlDocument filteredDoc = new XmlDocument();
                        filteredDoc.LoadXml("<temp>" + def + "</temp>");
                        definition = "<div style=\"font-size: 12px; text-aligh: justify;\">" + filteredDoc.DocumentElement.SelectSingleNode("/temp/def-definition").InnerXml + "</div>";
                    } else
                    {
                        definition = "<div style=\"font-size: 12px; text-aligh: justify;\">" + def + "</div>";
                    }
                }


                XNamespace rs = "http://cancergrid.org/schema/result-set";
                other = buildOtherContent();
                string alternate = buildAlternateContent(rs);
             
                if (sender.Equals(lstClassificationQueryResult))
                {
                    wbClassificationQueryResultDef.DocumentText = definition;
                }
                else if (sender.Equals(lstResults))
                {
                    wbDetailsDef.DocumentText = definition;
                    wbDetailsOther.DocumentText = other;
                    wbAltDetails.DocumentText = alternate;
                }
                
                if (vdNode != null)
                {
                    XElement x = XElement.Parse(vdNode.OuterXml);
                    if (x.Element(rs + "enumerated") != null && x.Element(rs + "enumerated").Element(rs + "valid-value") != null)
                    {

                        if (x.Elements(rs + "conceptCollection") != null)
                        {
                            var enumeratedValues = from ev in x.Element(rs + "enumerated").Elements(rs + "valid-value")
                                                   select new
                                                   {
                                                       Code = ev.Element(rs + "code").Value,
                                                       Meaning = ev.Element(rs + "meaning").Value,
                                                       ConceptCollection = ev.Element(rs + "conceptCollection")
                                                   };
                            values = "<table style=\"font-size: 12px;width: 100%;border: 1px solid #ddd;border-collapse: collapse;\"><tr><th style=\"background-color: #ddd;color: #000;text-align: left;padding: 5px;\">Value</th><th style=\"background-color: #ddd;color: #000;text-align: left;padding: 5px;\">Meaning</th><th style=\"background-color: #ddd;color: #000;text-align: left;padding: 5px;\">Concept</th></tr>";
                            foreach (var validValue in enumeratedValues)
                            {
                                //deal with concept collection
                                string conceptConcat = "";
                                if (validValue.ConceptCollection != null)
                                {
                                    var conColl = from cc in validValue.ConceptCollection.Elements(rs + "evsconcept")
                                                  orderby cc.Element(rs + "displayOrder").Value descending
                                                  select new
                                                  {
                                                      DisplayOrder = cc.Element(rs + "displayOrder").Value,
                                                      ConceptName = cc.Element(rs + "name").Value,
                                                      ConceptCode = cc.Element(rs + "code").Value
                                                  };
                                    foreach (var concept in conColl)
                                    {
                                        conceptConcat += ":" + concept.ConceptCode;
                                    }
                                    conceptConcat = conceptConcat.Substring(1);
                                }

                                values += "<tr><td style=\"border: 1px solid #ddd;padding: 5px;vertical-align: top;\">" + validValue.Code + "</td><td style=\"border: 1px solid #ddd;padding: 5px;vertical-align: top;\">" + validValue.Meaning + "</td><td style=\"border: 1px solid #ddd;padding: 5px;vertical-align: top;\">" + conceptConcat + "</td></tr>";
                            }
                            values += "</table>";

                        }
                        else
                        {
                            var enumeratedValues = from ev in x.Element(rs + "enumerated").Elements(rs + "valid-value")
                                                   select new
                                                   {
                                                       Code = ev.Element(rs + "code").Value,
                                                       Meaning = ev.Element(rs + "meaning").Value,
                                                   };
                            values = "<table style=\"font-size: 12px;width: 100%;border: 1px solid #ddd;border-collapse: collapse;\"><tr><th style=\"background-color: #ddd;color: #000;text-align: left;padding: 5px;\">Value</th><th style=\"background-color: #ddd;color: #000;text-align: left;padding: 5px;\">Meaning</th></tr>";
                            foreach (var validValue in enumeratedValues)
                            {
                                values += "<tr><td style=\"border: 1px solid #ddd;padding: 5px;vertical-align: top;\">" + validValue.Code + "</td><td style=\"border: 1px solid #ddd;padding: 5px;vertical-align: top;\">" + validValue.Meaning + "</td></tr>";
                            }
                            values += "</table>";

                        }

                    }
                    else if (x.Element(rs + "non-enumerated") != null)
                    {
                        values = "<table style=\"font-size: 12px;width: 100%;border: 1px solid #ddd;border-collapse: collapse;\">";
                        values += "<tr><th style=\"background-color: #ddd;color: #000;text-align: left;padding: 5px;\">data-type</th><td style=\"border: 1px solid #ddd;padding: 5px;vertical-align: top;\">" + x.Element(rs + "non-enumerated").Element(rs + "data-type").Value + "</td></tr>";
                        values += "<tr><th style=\"background-color: #ddd;color: #000;text-align: left;padding: 5px;\">units</th><td style=\"border: 1px solid #ddd;padding: 5px;vertical-align: top;\">" + x.Element(rs + "non-enumerated").Element(rs + "units").Value + "</td></tr>";
                        values += "</table>";
                    }


                    if (sender.Equals(lstClassificationQueryResult))
                    {
                        wbClassificationQueryResultValueDomain.DocumentText = values;
                    }
                    else if (sender.Equals(lstResults))
                    {
                        wbDetailsPropsValues.DocumentText = values;
                    }
                }
                else if (ccNode != null)
                {
                    XElement x = XElement.Parse(ccNode.OuterXml);
                    values = "<table style=\"font-size: 12px;width: 100%;border: 1px solid #ddd;border-collapse: collapse;\"><tr><th style=\"background-color: #ddd;color: #000;text-align: left;padding: 5px;\">Position</th><th style=\"background-color: #ddd;color: #000;text-align: left;padding: 5px;\">Name</th><th style=\"background-color: #ddd;color: #000;text-align: left;padding: 5px;\">Identifier</th></tr>";

                    var conceptCollection = from cc in x.Elements(rs + "evsconcept")
                                            orderby cc.Element(rs + "displayOrder").Value descending
                                            select new
                                            {
                                                DisplayOrder = cc.Element(rs + "displayOrder").Value,
                                                ConceptName = cc.Element(rs + "name").Value,
                                                ConceptCode = cc.Element(rs + "code").Value
                                            };

                    foreach (var concept in conceptCollection)
                    {
                        values += "<tr><td style=\"border: 1px solid #ddd;padding: 5px;vertical-align: top;\">";
                        values += ((Convert.ToInt16((string)concept.DisplayOrder)) == 0) ? "Primary" : "Qualifier";
                        values += "</td><td style=\"border: 1px solid #ddd;padding: 5px;vertical-align: top;\">";
                        values += concept.ConceptName;
                        values += "</td><td style=\"border: 1px solid #ddd;padding: 5px;vertical-align: top;\">";
                        values += concept.ConceptCode + "</td></tr>";
                    }

                    values += "</table>";
                    wbDetailsPropsValues.DocumentText = values;
                }
                else if (propsNode != null)
                {
                    XElement x = XElement.Parse(propsNode.OuterXml);
                    var properties = from p in x.Elements(rs + "property")
                                     orderby p.Element(rs + "name").Value
                                     select new
                                     {
                                         Name = p.Element(rs + "name").Value,
                                         Value = p.Element(rs + "value").Value
                                     };
                    props = "<table style=\"font-size: 12px;border: 1px solid #ddd;border-collapse: collapse;\"><tr><th style=\"background-color: #ddd;color: #000;text-align: left;padding: 5px;\">Name</th><th style=\"background-color: #ddd;color: #000;text-align: left;padding: 5px;\">Value</th></tr>";
                    
                    props += "<tr><td style=\"border: 1px solid #ddd;padding: 5px;vertical-align: top;\">ID</td><td style=\"border: 1px solid #ddd;padding: 5px;vertical-align: top;\">" + nodeId + "</td></tr>";
                       
                    foreach (var prop in properties)
                    {
                        props += "<tr><td style=\"border: 1px solid #ddd;padding: 5px;vertical-align: top;\">" + prop.Name + "</td><td style=\"border: 1px solid #ddd;padding: 5px;vertical-align: top;\">" + prop.Value + "</td></tr>";
                    }
                    props += "</table>";

                    if (sender.Equals(lstResults))
                    {
                        wbDetailsPropsValues.DocumentText = props;
                    }
                }
                else if (ocNode != null || propNode != null)
                {

                    props = "<table style=\"font-size: 12px;border: 1px solid #ddd;border-collapse: collapse;\">";
                    props += "<tr><th colspan=2 style=\"background-color: #ddd;color: #000;text-align: left;padding: 5px;\">Object Class</th></tr>";
                    props += formatBasicInfo(ocNode, rs);

                    props += "<tr><th colspan=2 style=\"background-color: #ddd;color: #000;text-align: left;padding: 5px;\">Property</th></tr>";

                    props += formatBasicInfo(propNode, rs);
                    props += "</table>";

                    if (sender.Equals(lstResults))
                    {
                        wbDetailsPropsValues.DocumentText = props;
                    }
                }
                else wbDetailsPropsValues.DocumentText = "No information retrieved";
                

            }
            catch (Exception ex)
            {
                MessageBox.Show(ex.Message);
            }
        }

        private string buildOtherContent()
        {
            XmlNode wfNode = lastResult.DocumentElement.SelectSingleNode("/rs:result-set/*[rs:names/rs:id = '" + getSelectedItem(lstResults).ID + "']/rs:workflow-status", nsmanager);
            XmlNode ctxNode = lastResult.DocumentElement.SelectSingleNode("/rs:result-set/*[rs:names/rs:id = '" + getSelectedItem(lstResults).ID + "']/rs:context", nsmanager);
            XmlNode regNode = lastResult.DocumentElement.SelectSingleNode("/rs:result-set/*[rs:names/rs:id = '" + getSelectedItem(lstResults).ID + "']/rs:registration-status", nsmanager);

            string other = "";

            if (wfNode == null || wfNode.InnerXml.Length == 0)
            {
                other = "<div style=\"font-size: 12px; text-aligh: justify;\">(No workflow status supplied)</div>";
            }
            else
            {
                other = "<div style=\"font-size: 12px; text-aligh: justify;\">Workflow Status: " + wfNode.InnerXml + "</div>";
            }

            if (regNode == null || regNode.InnerXml.Length == 0 || regNode.InnerXml.Length == 1)
            {
                other += "<p><div style=\"font-size: 12px; text-aligh: justify;\">(No registration status supplied)</div>";
            }
            else
            {
                other += "<p><div style=\"font-size: 12px; text-aligh: justify;\">Registration Status: " + regNode.InnerXml + "</div>";
            }

            if (ctxNode == null || ctxNode.InnerXml.Length == 0)
            {
                other += "<p><div style=\"font-size: 12px; text-aligh: justify;\">(No context supplied)</div>";
            }
            else
            {
                other += "<p><div style=\"font-size: 12px; text-aligh: justify;\">Context: " + ctxNode.InnerXml + "</div>";
            }

            return other;
        }

        private string buildAlternateContent(XNamespace rs)
        {
            string ret = "<p>";

           XmlNode altnameNode = lastResult.DocumentElement.SelectSingleNode("/rs:result-set/rs:concept[rs:names/rs:id = '" + getSelectedItem(lstResults).ID + "']/rs:names/rs:all-names", nsmanager);
           XmlNode altdefNode = lastResult.DocumentElement.SelectSingleNode("/rs:result-set/rs:concept[rs:names/rs:id = '" + getSelectedItem(lstResults).ID + "']/rs:all-definitions", nsmanager);

           if (altnameNode == null || altnameNode.InnerXml.Length == 0 || altnameNode.HasChildNodes == false)
           {
               ret = "<p><div style=\"font-size: 12px; text-aligh: justify;\">(No alternate names supplied)</div>";
           }
           else
           {
               XElement x = XElement.Parse(altnameNode.OuterXml);
               var properties = from p in x.Elements(rs + "name")
                                orderby p.Element(rs + "type").Value
                                select new
                                {
                                    Type = p.Element(rs + "type").Value,
                                    Value = p.Element(rs + "value").Value,
                                    Source = p.Element(rs+ "source").Value
                                };
               ret = "<p><table style=\"font-size: 12px;border: 1px solid #ddd;border-collapse: collapse;\"><tr><th style=\"background-color: #ddd;color: #000;colspan=\"3\";text-align: left;padding: 5px;\">Names</th></tr>";
                   
               ret+="<tr><th style=\"background-color: #ddd;color: #000;text-align: left;padding: 5px;\">Type</th><th style=\"background-color: #ddd;color: #000;text-align: left;padding: 5px;\">Source</th><th style=\"background-color: #ddd;color: #000;text-align: left;padding: 5px;\">Value</th></tr>";

               foreach (var prop in properties)
               {
                   ret += "<tr><td style=\"border: 1px solid #ddd;padding: 5px;vertical-align: top;\">" + prop.Type + "</td><td style=\"border: 1px solid #ddd;padding: 5px;vertical-align: top;\">" + prop.Source + "</td><td style=\"border: 1px solid #ddd;padding: 5px;vertical-align: top;\">" + prop.Value + "</td></tr>";
               }
               ret += "</table>";
           }

           if (altdefNode == null || altdefNode.InnerXml.Length == 0 || altdefNode.HasChildNodes == false)
           {
               ret += "<p><div style=\"font-size: 12px; text-aligh: justify;\">(No alternate definitions supplied)</div>";
           }
           else
           {
               XElement x = XElement.Parse(altdefNode.OuterXml);
               var properties = from p in x.Elements(rs + "definition")
                                orderby p.Element(rs + "type").Value
                                select new
                                {
                                    Type = p.Element(rs + "type").Value,
                                    Value = p.Element(rs + "value").Value,
                                    Source = p.Element(rs + "source").Value
                                };
               ret += "<p><table style=\"font-size: 12px;border: 1px solid #ddd;border-collapse: collapse;\"><tr><th style=\"background-color: #ddd;color: #000;colspan=\"3\";text-align: left;padding: 5px;\">Definitions</th></tr>";
               ret+="<tr><th style=\"background-color: #ddd;color: #000;text-align: left;padding: 5px;\">Type</th><th style=\"background-color: #ddd;color: #000;text-align: left;padding: 5px;\">Source</th><th style=\"background-color: #ddd;color: #000;text-align: left;padding: 5px;\">Value</th></tr>";

               foreach (var prop in properties)
               {
                   ret += "<tr><td style=\"border: 1px solid #ddd;padding: 5px;vertical-align: top;\">" + prop.Type + "</td><td style=\"border: 1px solid #ddd;padding: 5px;vertical-align: top;\">" + prop.Source + "</td><td style=\"border: 1px solid #ddd;padding: 5px;vertical-align: top;\">" + prop.Value + "</td></tr>";
               }
               ret += "</table>";
           }

           return ret;
        }

        private string formatBasicInfo(XmlNode node, XNamespace rs)
        {
            string ret = "";
            if (node != null)
            {
                XElement x = XElement.Parse(node.OuterXml);
                XElement namesNode = x.Element(rs + "names");

                string id = namesNode.Element(rs + "id").Value;
                string prefName = namesNode.Element(rs + "preferred").Value;
                var anames = from nm in namesNode.Elements(rs + "all-names")
                             select new
                             {
                                 Name = nm.Element(rs + "name")

                             };
                string td = "<td style=\"border: 1px solid #ddd;padding: 5px;vertical-align: top;\">";
                ret += "<tr>" + td + "ID:</td>" + td + id + "</td></tr>";
                ret += "<tr>" + td + "Preferred Name:</td>" + td + prefName + "</td></tr>";
                foreach (var nme in anames)
                {
                    if (!((string)nme.Name).Equals((string)prefName))
                        ret += "<tr>" + td + "Alt. Name:</td>" + td + nme.Name + "</td></tr>";
                }
            }
            else
            {
                string td = "<td style=\"border: 1px solid #ddd;padding: 5px;vertical-align: top;\">";
                ret += "<tr>" + td + "ID:</td>" + td + "(Not supplied)" + "</td></tr>";
                ret += "<tr>" + td + "Preferred Name:</td>" + td + "(Not supplied)" + "</td></tr>";
                ret += "<tr>" + td + "Alt. Name:</td>" + td + "(Not supplied)" + "</td></tr>";
                
            }

            return ret;
        }
        private void btnForwardCLS_Click(object sender, EventArgs e)
        {
            currentPageCLS++;
            //if (searchCLSWorker.IsBusy)
            //{
            //    searchCLSWorker.CancelAsync();
            //}
            //searchCLSWorker.RunWorkerAsync();
            searchCLS(sender, e);
        }

        private void btnSearchCLS_Click(object sender, EventArgs e)
        {
            currentPageCLS = 0;
            //if (searchCLSWorker.IsBusy)
            //{
            //    searchCLSWorker.CancelAsync();
            //}
            //searchCLSWorker.RunWorkerAsync();
            searchCLS(sender, e);
        }

        private void btnBackCLS_Click(object sender, EventArgs e)
        {
            currentPageCLS--;
            if (searchCLSWorker.IsBusy)
            {
                searchCLSWorker.CancelAsync();
            }
            searchCLSWorker.RunWorkerAsync();
            //searchCLS(sender, e);
        }

        delegate void SetStatusCallback(string text);
        delegate void SetErrorStatusCallback(string text);

        private void SetStatus(string text)
        {
            if (this.statusMsg.InvokeRequired)
            {
                SetStatusCallback callback = new SetStatusCallback(SetStatus);
                this.Invoke(callback, new object[] { text });
            }
            else
            {
                statusMsg.ForeColor = Color.Blue;
                statusMsg.Text = text;
                statusMsg.Update();
            }
        }

        private void SetErrorStatus(string text)
        {
            if (this.statusMsg.InvokeRequired)
            {
                SetErrorStatusCallback callback = new SetErrorStatusCallback(SetErrorStatus);
                this.Invoke(callback, new object[] { text });
            }
            else
            {
                statusMsg.ForeColor = Color.Red;
                statusMsg.Text = text;
                statusMsg.Update();
            }
        }

        private void grpDetails_Enter(object sender, EventArgs e)
        {

        }

        private void tableLayoutPanelResults_Paint(object sender, PaintEventArgs e)
        {

        }
    }

    public class QueryListItem
    {
        public string ID { get; set; }
        public string NAME { get; set; }

        public QueryListItem(string id, string name)
        {
            this.ID = id;
            this.NAME = name;
        }
    }

}
