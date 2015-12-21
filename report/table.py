import pandas as pd
import matplotlib.pyplot as plt
import math
import matplotlib
matplotlib.rcParams.update({'font.size': 16})
from scipy.stats import ttest_ind


# stats_path = "/Users/erotundo/git/UniNotes/ASO/statistics"
stats_path = "../statistics"

runs = ['hetero.1v1',
		'hetero.2v1',
		'hetero.3v1',
		'hetero.3v2.ho',
		'hetero.3v3.ho']

# public int generation;
#         public List<SubpopData> subpopData;
#         public int corralled;
#         public int escaped;
#         public int evaluations;
#         public double corralledRatio;
#         public double escapedRatio;
#         public double popMeanFitness;
#         public double popBestOfGeneration;
#         public double popBestSoFar;

# public double mean;
#         public double bestOfGeneration;
#         public double bestSoFar;

fitness_cols = {
	1: [2],
	2: [2, 5],
	3: [2, 5, 8]
}

for run in runs:
	num_sub_pop = int(run.split('.')[1].split('v')[0])
	print run
	df = pd.DataFrame()
	for i in range(1, 41):
		file_path = stats_path + str("/" + str(run) + "/" + str(i)+'.stat')		
		df_run = pd.read_csv(file_path, sep=' ', header=None, index_col=0, skipfooter=1, engine='python')		# print run
		dogs_fitness = df_run.iloc[:, fitness_cols[num_sub_pop]].mean(axis=1)
		mean_dog_fitness = dogs_fitness.mean()
		# print mean_dog_fitness
		mean_corralled_ratio = df_run.iloc[:, -5].mean()
		# print mean_corralled_ratio
		
		# THE FOLLOWING HAVE TO BE USED IN MUTUAL EXCLUSION
		df.loc[i, "fitness"] = mean_dog_fitness
		df.loc[i, "corralled_ratio"] = mean_corralled_ratio

	# print df
	# print df.mean(axis=1)
	print df.loc[:, "fitness"].describe()
	print df.loc[:, "corralled_ratio"].describe()
	print ""


# # hetero2vs1
# hetero2vs1 = pd.DataFrame()
# for i in range(1, 41):
# 	run_file = str(i)+'.stat'
# 	run = pd.read_csv("statistics/hetero.2v1/"+run_file, sep=' ', header=None, index_col=0)
# 	run = run.iloc[:,0:6].dropna()
# 	run.index.name = 'gen'
# 	run.columns = ['mean_dog1', 'bestOfGeneration_dog1', 'bestSoFar_dog1', 'mean_dog2', 'bestOfGeneration_dog2', 'bestSoFar_dog2']
# 	hetero2vs1[str(i)+"_1"] = run['bestSoFar_dog1']
# 	hetero2vs1[str(i)+"_2"] = run['bestSoFar_dog2']
# hetero2vs1['std_error'] = hetero2vs1.std(axis=1) / math.sqrt(80)
# hetero2vs1['ci_high'] =  (1.96 * hetero2vs1['std_error'])
# hetero2vs1['ci_low'] =  (1.96 * hetero2vs1['std_error'])


## PRINT
# fig, ax = plt.subplots(nrows=1, ncols=1)
# ax.errorbar(homo2vs1.index, homo2vs1.mean(axis=1).values, yerr=pd.concat([homo2vs1['ci_low'], 
# 	homo2vs1['ci_high']], axis=1).T.values, fmt='o-', label='Homo')
# ax.errorbar(hetero2vs1.index, hetero2vs1.mean(axis=1).values, yerr=pd.concat([hetero2vs1['ci_low'],hetero2vs1['ci_high']], axis=1).T.values, 
# 	fmt='o-', label='Hetero')
# plt.ylim((0, 70000))
# fig.suptitle('Homo vs Hetero - 2vs1 - 40 runs')
# legend = ax.legend(loc='lower right', shadow=True)
# ax.set_xlabel('Generation')
# ax.set_ylabel('Fitness')
# fig.tight_layout(pad=2)
# plt.savefig("/Users/erotundo/git/evolutionary-shepherding/report/imgs/homo2v1-hetero2v1-bestSoFar.pdf")

## T-TEST
# print ttest_ind(homo2vs1.mean(axis=1).values, hetero2vs1.mean(axis=1).values)
