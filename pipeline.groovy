//Sample pipeline code with "stage-1", we can add as many stages as we want.


def getBuildnode(){
	//Getting build node name is hard with Jenkins pipeline, this function will help in finding it
  echo 'Running Build on...'
  sh "hostname"
}


def sourcecheckout(){
	// Basic branch checkout
  checkout([
  $class: 'GitSCM',
  changelog: true,
  branches: scm.branches,
  extensions: scm.extensions
  userRemoteConfigs: scm.userRemoteConfigs
  ])
}


/**
 * Generate JSON with different Build status
 */
def buildJson(){
  withCredentials([usernamePassword(credentialsId: 'Bitbucket', passwordVariable: 'passwd', usernameVariable: 'username')]) {
      sh '''
        cd ${WORKSPACE}
        python generator.py "${BUILD_ID}" "${JOB_BASE_NAME}" "${RUN_DISPLAY_URL}" "stage-1"
        echo "$(git ls-remote https://"${username}":"${passwd}"@<BITBUCKET_URL> refs/heads/$BRANCH_NAME | awk {'print $1'})" > commit.json
      '''
  }
  stash name:"json-files", includes: "*.json"                        
}


/**
 * Notify Bitbucket Build status
 * @param status
 * @param stage
 */ 
def nofity_bitbucket(status, stage){
  sh '''
  GIT_COMMIT=$(cat commit.json | tail -1)
  curl -k --retry 3 --cert /home/${user}/public-cert.pem --key /home/${user}/key.pem --cert-type pem -u "${username}:${passwd}" -H "Content-Type: application/json" -X POST https://<BITBUCKET_URL>/rest/build-status/1.0/commits/${GIT_COMMIT} -d @'''+status+'''-'''+stage+'''.json
  '''
}


pipeline {

  agent {
    node { label 'build'}
  }

  stages {
  	//All stages
    	stage ("stage-1") {
    	// Initializing checkout, node name
      		steps {
        		getBuildnode()
        		buildJson()
        		dir("${env.WORKSPACE}"){
          			withCredentials([usernamePassword(credentialsId: 'Bitbucket', passwordVariable: 'passwd', usernameVariable: 'username')]) {
            			unstash "json-files"
            			// Notifying Bitbucket about start of the build or stage
            			nofity_bitbucket("INPROGRESS", "stage-1")
          			}
        		}
        		sourcecheckout()
      		}

      		post {
      			// Build status notification to Bitbucket PR
        		failure {
          			script {       
            			dir("${env.WORKSPACE}"){
              				sh "echo 'Jenkins job has failed'"
              				withCredentials([usernamePassword(credentialsId: 'Bitbucket', passwordVariable: 'passwd', usernameVariable: 'username')]){
                				nofity_bitbucket("FAILED", "stage-1")
              				}
            			}
          			}
        		}
        		success {
        			script {       
            			dir("${env.WORKSPACE}"){
              				sh "echo 'Jenkins job has failed'"
              				withCredentials([usernamePassword(credentialsId: 'Bitbucket', passwordVariable: 'passwd', usernameVariable: 'username')]){
                				nofity_bitbucket("SUCCESSFUL", "stage-1")
              				}
            			}
          			}
        		}
        		unstable {
        			script {       
            			dir("${env.WORKSPACE}"){
              				sh "echo 'Jenkins job has failed'"
              				withCredentials([usernamePassword(credentialsId: 'Bitbucket', passwordVariable: 'passwd', usernameVariable: 'username')]){
                				nofity_bitbucket("SUCCESSFUL", "stage-1")
              				}
            			}
          			}
        		}
        		aborted {
        			script {       
            			dir("${env.WORKSPACE}"){
              				sh "echo 'Jenkins job has failed'"
              				withCredentials([usernamePassword(credentialsId: 'Bitbucket', passwordVariable: 'passwd', usernameVariable: 'username')]){
                				nofity_bitbucket("FAILED", "stage-1")
              				}
            			}
          			}        		
        		}
      		}
    	}
	}
}