# Upgrade Galahad from 1.x.x to 2.0.0.
# 
# In 2.0.0, lemma and pos were moved from Term.lemma and Term.pos to Term.annotation["lemma"] and Term.annotation["pos"]
# in order to allow for more annotations to be added to a Term.
# 
# This upgrade script parses all json in the ARG1 folder and updates the json to the new format.

import os
import sys
import json

def upgrade_corpora(folder):
    # list all corpora (folders) in the ARG1 folder
    corpora = os.listdir(folder)
    total = len(corpora)
    print(f"Upgrading [{total}] corpora")
    for i, corpus in enumerate(corpora):
        print(f"[{i+1}/{total}] Upgrading {corpus}")
        corpus_folder = os.path.join(folder, corpus)
        upgrade_jobs(corpus_folder)
        upgrade_docs(corpus_folder)
        print()

def upgrade_jobs(corpus):
    # list all jobs in the corpus/jobs/ folder
    jobs = os.listdir(os.path.join(corpus, "jobs"))
    num_jobs = len(jobs)
    print(f"\tUpgrading [{num_jobs}] jobs")
    for job_i, job in enumerate(jobs):
        print(f"\t\t[{job_i+1}/{num_jobs}] Upgrading {job}")
        # list all documents in the corpus/jobs/job/documents/ folder
        documents = os.listdir(os.path.join(corpus, "jobs", job, "documents"))
        num_docs = len(documents)
        print(f"\t\tUpgrading {num_docs} job documents")
        for doc_i, doc in enumerate(documents):
            print(f"\t\t[{doc_i+1}/{num_docs}] Upgrading {doc}")
            # for each document folder, try to access corpus/jobs/job/documents/document/result
            json_path = os.path.join(corpus, "jobs", job, "documents", doc, "result")
            upgrade_json(json_path)


def upgrade_docs(corpus):
    # list all docs in the corpus/documents/ folder
    documents = os.listdir(os.path.join(corpus, "documents"))
    num_docs = len(documents)
    print(f"\tUpgrading [{num_docs}] documents")
    for doc_i, doc in enumerate(documents):
        print(f"\t\t[{doc_i+1}/{num_docs}] Upgrading {doc}")
        json_path = os.path.join(corpus, "documents", doc, "sourceLayer")
        upgrade_json(json_path)

def upgrade_json(path):
    if os.path.exists(path):
        # read the json file
        with open(path, "r") as file:
            data = json.load(file)                    
        # update the terms on the root
        for term in data["terms"]:
            upgrade_term(term)
        # update the terms in preview
        for term in data["preview"]["terms"]:
            upgrade_term(term)
        # write the updated json back to the file
        with open(path, "w") as file:
            json.dump(data, file)
        print(f"\t\tSuccefully upgraded")
    else:
        print(f"\t\tJson absent. No need to upgrade")

def upgrade_term(term):
    if "lemma" in term or "pos" in term:
        term["annotations"] = {
            "lemma": term.pop("lemma"),
            "pos": term.pop("pos")
        }

if __name__ == "__main__":
    # Usage
    if len(sys.argv) != 2:
        app_name = sys.argv[0]
        print(f"Usage: python3 {app_name} [folder]")
        sys.exit(1)
    
    folder = sys.argv[1]
    upgrade_corpora(folder)
    # remove caches
    print("Removing all caches")
    os.system(f"find {folder} -type f -name '*.cache' -delete")
