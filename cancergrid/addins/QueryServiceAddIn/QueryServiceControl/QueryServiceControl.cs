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
                query.numResults = pageSize + 1;
                if (currentPage > 0)
                    query.numResults = 200;

                string response = qsm.queryString(query);

                lastResult = new XmlDocument();
                lastResult.LoadXml(response);

                nsmanager = new XmlNamespaceManager(lastResult.NameTable);
                nsmanager.AddNamespace("rs", "http://cancergrid.org/schema/result-set");

                int rstSize = 6;
                string[] resultSetTypes = new string[rstSize];
                resultSetTypes[0] = "data-element";
                resultSetTypes[1] = "concept";
                resultSetTypes[2] = "object-class";
                resultSetTypes[3] = "property";
                resultSetTypes[4] = "conceptual-domain";
                resultSetTypes[5] = "representation-term";

                XmlNodeList nodeList = null;
                for (int i = 0; i < rstSize; i++)
                {
                    nodeList = lastResult.DocumentElement.SelectNodes("/rs:result-set/rs:"+resultSetTypes[i], nsmanager);
                    if (i > 0)
                        btnAnnotate.Enabled = false;
                    else
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

                listResults(nodeList, lstResults);
                btnUse.Enabled = true;

                if (currentPage > 0)
                {
                    btnBack.Enabled = true;
                }

                if (nodeList.Count >= pageSize)
                {
                    btnForward.Enabled = true;
                }

                //statusMsg.Text = "Query completed.";
                SetStatus("Query completed");
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
            foreach (XmlNode node in results)
            {
                string id = node.SelectSingleNode("rs:names/rs:id", nsmanager).InnerXml;
                string name = node.SelectSingleNode("rs:names/rs:preferred", nsmanager).InnerXml;

                if (node.SelectSingleNode("rs:workflow-status", nsmanager) != null)
                {
                    string workflow = node.SelectSingleNode("rs:workflow-status", nsmanager).InnerXml;
                    string registration = node.SelectSingleNode("rs:registration-status", nsmanager).InnerXml;

                    target.Items.Add(new QueryListItem(id, name + "  (" + registration + ":" + workflow + ")"));
                }
                else
                {
                    target.Items.Add(new QueryListItem(id, name));
                
                }
            }
            if (target.Items.Count == pageSize + 1)
            {
                target.Items.RemoveAt(pageSize);
            }
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
                string workflow = null;
                XmlNode defNode = null;
                XmlNode propsNode = null;
                XmlNode vdNode = null;
                XmlNode wfNode = null;
                XmlNode ccNode = null;
                
                if (sender.Equals(lstClassificationQueryResult))
                {
                    vdNode = lastClassificationQueryResult.DocumentElement.SelectSingleNode("/rs:result-set/*[rs:names/rs:id = '" + getSelectedItem(lstClassificationQueryResult).ID + "']/rs:values", nsmanager);
                    defNode = lastClassificationQueryResult.DocumentElement.SelectSingleNode("/rs:result-set/*[rs:names/rs:id = '" + getSelectedItem(lstClassificationQueryResult).ID + "']/rs:definition", nsmanager);
                }
                else if (sender.Equals(lstResults))
                {
                    wfNode = lastResult.DocumentElement.SelectSingleNode("/rs:result-set/*[rs:names/rs:id = '" + getSelectedItem(lstResults).ID + "']/rs:workflow-status", nsmanager);
                    vdNode = lastResult.DocumentElement.SelectSingleNode("/rs:result-set/rs:data-element[rs:names/rs:id = '" + getSelectedItem(lstResults).ID + "']/rs:values", nsmanager);
                    propsNode = lastResult.DocumentElement.SelectSingleNode("/rs:result-set/rs:concept[rs:names/rs:id = '" + getSelectedItem(lstResults).ID + "']/rs:properties", nsmanager);
                    defNode = lastResult.DocumentElement.SelectSingleNode("/rs:result-set/*[rs:names/rs:id = '" + getSelectedItem(lstResults).ID + "']/rs:definition", nsmanager);
                    ccNode = lastResult.DocumentElement.SelectSingleNode("/rs:result-set/*[rs:names/rs:id = '" + getSelectedItem(lstResults).ID + "']/rs:conceptCollection", nsmanager);
                    if (ccNode == null || ccNode.InnerXml.Length == 0)
                        ccNode = lastResult.DocumentElement.SelectSingleNode("/rs:result-set/rs:property[rs:names/rs:id = '" + getSelectedItem(lstResults).ID + "']/rs:conceptCollection", nsmanager);              
                }
                else
                {
                    return;
                }
                if (defNode == null || defNode.InnerXml.Length == 0)
                {
                    definition = "<div style=\"font-size: 14px; text-aligh: justify;\">(No definition supplied)</div>";
                }
                else
                {
                    string def = defNode.InnerXml;
                    if (def.Contains("def-source"))
                    {
                        def = def.Trim().Replace("&gt;", ">").Replace("&lt;", "<").Replace("<![CDATA[", "").Replace("]]>", "");
                        XmlDocument filteredDoc = new XmlDocument();
                        filteredDoc.LoadXml("<temp>" + def + "</temp>");
                        definition = "<div style=\"font-size: 14px; text-aligh: justify;\">" + filteredDoc.DocumentElement.SelectSingleNode("/temp/def-definition").InnerXml + "</div>";
                    } else
                    {
                        definition = "<div style=\"font-size: 14px; text-aligh: justify;\">" + def + "</div>";
                    }
                }


                if (wfNode == null || wfNode.InnerXml.Length == 0)
                {
                    workflow = "<div style=\"font-size: 14px; text-aligh: justify;\">(No workflow status supplied)</div>";
                }
                else
                {
                    workflow = "<div style=\"font-size: 14px; text-aligh: justify;\">Workflow Status: " + wfNode.InnerXml + "</div>";
                }


                if (sender.Equals(lstClassificationQueryResult))
                {
                    wbClassificationQueryResultDef.DocumentText = definition;
                }
                else if (sender.Equals(lstResults))
                {
                    wbDetailsDef.DocumentText = definition;
                    wbDetailsOther.DocumentText = workflow;
                }

                XNamespace rs = "http://cancergrid.org/schema/result-set";
                if (vdNode != null)
                {
                    XElement x = XElement.Parse(vdNode.OuterXml);
                    if (x.Element(rs + "enumerated") != null && x.Element(rs + "enumerated").Element(rs + "valid-value") != null)
                    {

                        if (x.Element(rs + "enumerated").Element(rs + "valid-value").Element(rs + "conceptCollection") != null)
                        {
                            var enumeratedValues = from ev in x.Element(rs + "enumerated").Elements(rs + "valid-value") 
                                                   select new
                                                   {
                                                       Code = ev.Element(rs + "code").Value,
                                                       Meaning = ev.Element(rs + "meaning").Value,
                                                       ConceptCollection = ev.Element(rs + "conceptCollection")
                                                   };
                            values = "<table style=\"width: 100%;border: 1px solid #ddd;border-collapse: collapse;\"><tr><th style=\"background-color: #ddd;color: #000;text-align: left;padding: 5px;\">Code</th><th style=\"background-color: #ddd;color: #000;text-align: left;padding: 5px;\">Meaning</th><th style=\"background-color: #ddd;color: #000;text-align: left;padding: 5px;\">Concept</th></tr>";
                            foreach (var validValue in enumeratedValues)
                            {
                                //deal with concept collection
                                string conceptConcat = "";
                                if (validValue.ConceptCollection != null)
                                {
                                    var conColl = from cc in validValue.ConceptCollection.Elements(rs+"evsconcept")
                                                        orderby cc.Element(rs+"displayOrder").Value descending
                                                        select new {
                                                            DisplayOrder = cc.Element(rs+"displayOrder").Value,
                                                            ConceptName = cc.Element(rs+"name").Value
                                                        };
                                    foreach (var concept in conColl)
                                    {
                                        conceptConcat += ":" + concept.ConceptName;
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
                            values = "<table style=\"width: 100%;border: 1px solid #ddd;border-collapse: collapse;\"><tr><th style=\"background-color: #ddd;color: #000;text-align: left;padding: 5px;\">Code</th><th style=\"background-color: #ddd;color: #000;text-align: left;padding: 5px;\">Meaning</th></tr>";
                            foreach (var validValue in enumeratedValues)
                            {
                                values += "<tr><td style=\"border: 1px solid #ddd;padding: 5px;vertical-align: top;\">" + validValue.Code + "</td><td style=\"border: 1px solid #ddd;padding: 5px;vertical-align: top;\">" + validValue.Meaning + "</td></tr>";
                            }
                            values += "</table>";

                        }

                    }
                    else if (x.Element(rs + "non-enumerated") != null)
                    {
                        values = "<table style=\"width: 100%;border: 1px solid #ddd;border-collapse: collapse;\">";
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
                    values = "<table style=\"width: 100%;border: 1px solid #ddd;border-collapse: collapse;\"><tr><th style=\"background-color: #ddd;color: #000;text-align: left;padding: 5px;\">Position</th><th style=\"background-color: #ddd;color: #000;text-align: left;padding: 5px;\">Concept</th></tr>";
                    
                    var conceptCollection = from cc in x.Elements(rs + "evsconcept")
                                            orderby cc.Element(rs + "displayOrder").Value descending
                                            select new
                                            {
                                                DisplayOrder = cc.Element(rs + "displayOrder").Value,
                                                ConceptName = cc.Element(rs + "name").Value,
                                            };

                    foreach (var concept in conceptCollection)
                    {
                        values += "<tr><td style=\"border: 1px solid #ddd;padding: 5px;vertical-align: top;\">";
                        values += ((Convert.ToInt16((string)concept.DisplayOrder)) == 0) ? "Primary" : "Qualifier";
                        values += "</td><td style=\"border: 1px solid #ddd;padding: 5px;vertical-align: top;\">";
                        values += concept.ConceptName + "</td></tr>";

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
                    props = "<table style=\"border: 1px solid #ddd;border-collapse: collapse;\"><tr><th style=\"background-color: #ddd;color: #000;text-align: left;padding: 5px;\">Name</th><th style=\"background-color: #ddd;color: #000;text-align: left;padding: 5px;\">Value</th></tr>";
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
            }
            catch (Exception ex)
            {
                MessageBox.Show(ex.Message);
            }
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
